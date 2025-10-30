## Nimbbl Core API SDK – Versioning and Publishing

This guide covers how to bump the SDK version and publish builds (snapshot and stable) to JitPack using the scripts in this repo.

### Prerequisites
- Git clean working tree on the `master` branch (the publish script enforces this).
- `version.properties` present and up to date.
- Java/Gradle installed locally (for local builds).
- JitPack access to the repository (for private repos).

### Files
- `version_manager.sh`: Utilities to bump `version.properties` and manage release notes.
- `publish_to_jitpack.sh`: Tags the repo and triggers JitPack builds; prints the dependency line to use.

### Environment
Export these env vars in your shell/session (add to your shell profile if preferred):

```bash
export GITHUB_USERNAME=nimbbl-tech
export REPO_NAME=nimbbl_mobile_kit_core_api_sdk
export JITPACK_API_TOKEN=your_jitpack_token  # optional, not required for public repos
```

### Bump Version (optional but recommended)
Use `version_manager.sh` to update `SDK_VERSION` in `version.properties`.

Common flows:
```bash
# Show current version
./version_manager.sh --show

# Bump patch/minor/major
./version_manager.sh --bump patch
./version_manager.sh --bump minor
./version_manager.sh --bump major

# Edit release notes section interactively (if supported)
./version_manager.sh --notes
```

Commit the changes when satisfied:
```bash
git add version.properties
git commit -m "chore(version): bump to $(grep '^SDK_VERSION=' version.properties | cut -d'=' -f2)"
```

### Publish a Snapshot (recommended first)
Snapshot builds are great for pre-release validation.

```bash
# Must be on master branch
./publish_to_jitpack.sh --snapshot
```

This creates a tag like `vX.Y.Z-SNAPSHOT-YYYYMMDD-HHMMSS-<hash>` and prints the dependency line:

```groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies {
    implementation 'com.github.nimbbl-tech:nimbbl-checkout-core-sdk:vX.Y.Z-SNAPSHOT-YYYYMMDD-HHMMSS-<hash>'
}
``;

### Publish a Stable Release
Two ways to tag and publish a stable build:

```bash
# Use the version currently in version.properties
./publish_to_jitpack.sh --current

# Or specify explicitly
./publish_to_jitpack.sh vX.Y.Z
```

On success, the script prints the dependency line:

```groovy
repositories { maven { url 'https://jitpack.io' } }
dependencies {
    implementation 'com.github.nimbbl-tech:nimbbl-checkout-core-sdk:vX.Y.Z'
}
```

### Private Repositories (optional)
If the repo is private, set in `.env`:

```bash
REPO_IS_PRIVATE=true
JITPACK_TRIGGER_BUILD=true
JITPACK_API_TOKEN=your_jitpack_token
```

This enables an optional manual trigger to JitPack’s API. You can also manage access at `https://jitpack.io`.

### Notes
- The artifactId is defined in the module POM as `nimbbl-checkout-core-sdk` and is used by the publish script when printing the Gradle dependency.
- Ensure release builds succeed locally (`./gradlew clean assembleRelease`) before tagging.
- Keep mapping files under `build/outputs/mapping/release/` archived for each release.


