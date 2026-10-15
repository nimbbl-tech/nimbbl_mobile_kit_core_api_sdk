# Maven Central Deployment

How to publish `tech.nimbbl:core-api-sdk` to Maven Central via the Sonatype Central Portal,
using [`publish_to_maven_central.sh`](publish_to_maven_central.sh).

This is a separate, independent publish target from JitPack — see
[README_PUBLISHING.md](README_PUBLISHING.md) for that. You can use either or both; they don't
interfere with each other.

## One-time setup

### 1. Sonatype Central Portal account + namespace

- Create an account at [central.sonatype.com](https://central.sonatype.com) if you don't have one.
- Claim and **verify** the `tech.nimbbl` namespace under **Publish → Namespaces**. For a
  reverse-domain group ID like this, verification is via a DNS TXT record on `nimbbl.tech` that
  Sonatype gives you — this can take a little time to propagate and confirm.
- **Uploads will fail with `403 Forbidden` until this namespace shows as verified**, even with
  otherwise-correct credentials. This bit us during the first real deployment of this script —
  the account's credentials were valid, but the namespace wasn't associated with the account yet.

### 2. Generate a Central Portal User Token

Go to **central.sonatype.com → Account → Generate User Token**. This gives you a
username/password-shaped pair — **not your real account login email/password**:

```xml
<server>
  <id>central</id>
  <username>a1B2c3D4</username>
  <password>eF5gH6iJ7kL8mN9oP0qR1sT2</password>
</server>
```

This is the single most common setup mistake: pasting your actual account credentials here
instead of a generated token fails with `401 Unauthorized` / `{"error":{"message":"Invalid
token"}}`. If you ever revoke and regenerate the token, update `.env` with the new values —
the old ones stop working immediately.

### 3. GPG signing key

Maven Central requires every artifact to be GPG-signed.

```bash
# If you don't have a key yet:
gpg --full-generate-key

# Find your key ID (the short, 8-char one — e.g. BCA404DC, not the 40-char fingerprint):
gpg --list-secret-keys --keyid-format SHORT
# Use the `sec` line's ID, not the `ssb` (encryption subkey) line's ID.

# Export it to a file the build can read (you'll be prompted for your passphrase —
# on macOS this is a native pinentry popup, not a terminal prompt, so watch your screen):
gpg --export-secret-keys <keyId> > signing-key.gpg
```

If the passphrase prompt is cancelled or times out, `signing-key.gpg` is written but **empty**
(0 bytes) rather than erroring loudly — `publish_to_maven_central.sh` checks for this and will
refuse to proceed, but check `ls -la signing-key.gpg` yourself if something seems off.

### 4. `.env`

Copy `.env.example` to `.env` and fill in all five fields:

```bash
centralPortalUsername=       # from step 2 (the generated token's username, not your login)
centralPortalPassword=       # from step 2 (the generated token's password)
signing.keyId=               # from step 3 (short 8-char form)
signing.password=            # your GPG key's passphrase
signing.secretKeyRingFile=   # absolute path to the file from step 3
```

`.env` and `signing-key.gpg` are both already in `.gitignore`. Never commit them.
`build.gradle.kts` loads `.env` itself at configure time, so the script never needs to pass
these as `-P` flags (which would otherwise be visible in `ps`/shell history).

## Using the script

```bash
./publish_to_maven_central.sh --help
```

| Flag | Meaning |
|---|---|
| `--dry-run` (default) | Build, sign, install to `~/.m2` only. No network upload. |
| `--publish` | Upload for real. |
| `--release-type release` (default) | Publish `SDK_VERSION` as-is. **Immutable once accepted — cannot be deleted or overwritten.** |
| `--release-type snapshot` | Publish `SDK_VERSION-SNAPSHOT` to the snapshot repo. Freely re-uploadable — use this to rehearse the real upload path before committing to a release. |
| `--bump <patch\|minor\|major>` | Runs `version_manager.sh bump` first. Needed whenever the current `SDK_VERSION` is already published (Central won't accept a duplicate release version). |
| `--tests <none\|unit\|all>` (default: `unit`) | Pre-publish test scope. `all` also runs `connectedDebugAndroidTest`, which needs a connected device/emulator. |
| `--yes` | Skip the interactive confirmation (for CI, or non-interactive shells — see note below). |
| `--verify-staging` | Real network build+sign+upload+finalize of a **release** build, but finalized as `user_managed` instead of `automatic` and then **dropped** before it can go live. Verifies the entire pipeline, including the finalize step, without ever publishing anything. No git check, no tag. See "Verifying without actually publishing" below. |

### Commit/push requirement, and tagging (release type only)

For `--release-type release --publish`, the script hard-requires the working tree to be fully
**committed and pushed** before it will build anything:
- No uncommitted changes to tracked files (untracked files are only warned about, not blocked on).
- The current branch must have an upstream, and must not be ahead of it (i.e. everything's pushed).

This is **not** skippable with `--yes` — that flag only skips the interactive upload
confirmation, not this correctness check. The reasoning: a release artifact is immutable and
should always correspond to an exact, shareable commit someone else could check out.

On a **successful** release upload, the script tags that exact commit `vX.Y.Z` (matching
`publish_to_jitpack.sh`'s tag convention) and pushes the tag. Snapshot publishes skip both the
requirement and the tagging — they're explicitly for iterating before you're ready to commit to
either.

### Recommended workflow

```bash
# 1. Rehearse the whole pipeline locally — no network call.
./publish_to_maven_central.sh

# 2. Rehearse the real upload path with a snapshot (safe — snapshots are re-uploadable).
./publish_to_maven_central.sh --release-type snapshot --publish

# 3. Rehearse the RELEASE upload + finalize path for real, without publishing anything.
./publish_to_maven_central.sh --publish --verify-staging

# 4. Bump if the current version.properties is already published, then release for real.
./publish_to_maven_central.sh --bump patch --publish
```

### Verifying without actually publishing

A snapshot publish (step 2 above) rehearses the upload, but snapshots skip the finalize step
entirely — they don't prove the release-only finalize call works. There's no release-type
equivalent of "just upload, don't publish" via Gradle alone, because a plain
`publishReleasePublicationToSonatypeRepository` only ever reaches the OSSRH Staging API's
staging area — it doesn't even become visible in the Portal until finalized (see "automatic
finalize" above).

`--verify-staging` closes that gap. It runs the exact same release build/sign/upload as a real
release, but finalizes with `publishing_type=user_managed` instead of `automatic`. That lands
the deployment as a **pending, reviewable staging repository** in the Portal instead of
auto-releasing it — nothing is published until someone explicitly clicks "Publish" (or another
`publishing_type=automatic` finalize call happens). The script then looks that repository up via
`GET /manual/search/repositories` and drops it via `DELETE /manual/drop/repository/<key>`, so it
never reaches `repo1.maven.org`.

```bash
./publish_to_maven_central.sh --publish --verify-staging
```

This is real network activity against your actual Sonatype account/namespace/credentials — it
is not a local-only simulation — but nothing it does is irreversible: a user_managed deployment
that's never published can always be dropped, which is exactly what this does automatically
when there's exactly one open staging repository. If there's more than one (ambiguous — could be
someone else's in-flight deployment), it prints the list and tells you the exact `curl` command
to drop the right one yourself rather than guessing.

No git check and no tag happen for `--verify-staging` — nothing was actually released, so there's
nothing to tag.

### Non-interactive shells and `--yes`

The script's confirmation prompt (`read -p ...`) needs a real, dedicated stdin. If you run it in
a context where stdin is shared with the subprocess tree (e.g. `echo "y" | ./publish_to_maven_central.sh ...`),
the "y" can get silently consumed by something else in the chain (we saw Gradle's own interactive
prompts do this once) before it reaches the script's own `read` — the script then sees EOF, and
`set -e` causes it to exit with **no error message and no indication anything went wrong**. If a
run seems to stop right after the "About to upload..." line with nothing further, this is almost
certainly what happened. Use `--yes` instead of piping input.

## Verifying a deployment

The browser UI (`central.sonatype.com`'s "browse" views) is a client-rendered app — it returns
the same HTML shell for every URL when fetched without an authenticated browser session, so
`curl` (and sometimes a not-logged-in browser) won't show real content there. Use these instead:

```bash
# 1. Metadata file — lists every published file + the real timestamped build number
curl -s "https://central.sonatype.com/repository/maven-snapshots/tech/nimbbl/core-api-sdk/<version>-SNAPSHOT/maven-metadata.xml"

# 2. Direct file fetch — use the <value> from the metadata above, which already includes the
#    version prefix (format: <version>-<timestamp>-<buildNumber>, e.g. 4.0.14-20261007.060315-1)
curl -I "https://central.sonatype.com/repository/maven-snapshots/tech/nimbbl/core-api-sdk/<version>-SNAPSHOT/core-api-sdk-<value>.aar"

# 3. Strongest proof: actually resolve it as a real Gradle dependency in a throwaway project
#    repositories { maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") } }
#    dependencies { implementation("tech.nimbbl:core-api-sdk:<version>-SNAPSHOT") }
```

A released (non-snapshot) version eventually appears at
`https://repo1.maven.org/maven2/tech/nimbbl/core-api-sdk/<version>/` — but see the note below
about pending deployments first.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `401 Unauthorized`, `{"error":{"message":"Invalid token"}}` | `centralPortalUsername`/`Password` aren't a generated User Token (e.g. real account login was used instead) | Regenerate a token at central.sonatype.com → Account → Generate User Token, update `.env` |
| `403 Forbidden` on the upload PUT | `tech.nimbbl` namespace not verified/associated with this account yet | Verify the namespace (see step 1); nothing to fix in the script or build |
| Script exits right after "About to upload..." with no error | A piped confirmation answer was consumed by something else in the process tree before reaching the script's prompt | Use `--yes` instead of piping `echo "y" | ...` |
| `MockitoException: Cannot mock/spy ... final class` *(unrelated to publishing, but if you see it while running `--tests all`)* | See [TESTING.md](TESTING.md)'s "Mocking the HTTP layer" section | N/A — this is expected architecture, not a bug |
| Confusingly slow/cryptic Gradle error on auth or permission issues | Gradle's own error messages for HTTP failures are terse | Run the same request directly with `curl -u "$user:$pass" ...` against the failing URL for the real server-side error body (see examples used while debugging this script — Gradle only ever showed the HTTP status code, `curl` showed the actual JSON error) |
| `"There are uncommitted changes to tracked files"` on `--release-type release --publish` | Hard requirement — see "Commit/push requirement" above | Commit everything, or use `--release-type snapshot` to rehearse without committing |
| `"Current branch has no upstream tracking branch"` | Branch was never pushed | `git push -u origin <branch>` |
| `"Local branch has N unpushed commit(s)"` | Commits exist locally that aren't on the remote | `git push` |
| `"Could not auto-finalize the deployment (HTTP ...)"` after a release upload | The finalize POST to the OSSRH Staging API failed | Finalize manually at `https://central.sonatype.com/publishing/deployments` — see "automatic finalize" section below |

## After a real release upload succeeds: automatic finalize

There is no namespace-level "Automatic Publishing" toggle in the Central Portal UI — we looked
for one and it doesn't exist. Instead, publishing is controlled per-request. Our Gradle
`maven-publish` upload only lands the build in the **OSSRH Staging API** compatibility service
(`ossrh-staging-api.central.sonatype.com`) — it never shows up in the Central Portal, and
therefore never auto-releases, until it's explicitly **finalized** into the Portal.

`publish_to_maven_central.sh` does this automatically for `--release-type release --publish`,
right after the upload succeeds, via `finalize_ossrh_deployment_to_portal()`:

```bash
POST https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/tech.nimbbl?publishing_type=automatic
Authorization: Bearer <base64(username:password)>   # the same Central Portal User Token from .env
```

(Reference: [OSSRH Staging API docs](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/).)
`publishing_type=automatic` tells the Portal to auto-release as soon as Central's validation
passes, with no manual click needed. This call must run from the same network/session as the
upload — a single script run naturally satisfies that.

- On success (HTTP 2xx), the script prints that it's finalized and will appear at
  `repo1.maven.org` once validation passes (minutes to a few hours).
- On failure, the script falls back to telling you to finalize manually at
  `https://central.sonatype.com/publishing/deployments`.

Snapshot publishes skip this entirely — the snapshot repository is live-browsable immediately
and has no staging/finalize step.

**Note:** this finalize call has only been syntax-checked (`bash -n`) so far, not exercised for
real in this project yet — the only real publish done so far was a `4.0.14-SNAPSHOT`. Use
`--verify-staging` (below) to exercise the finalize call for real without committing to an actual
release.

Maven Central release versions are **immutable** — once live, that exact version can never be
deleted or overwritten. If something's wrong, the only fix is publishing a new version.
