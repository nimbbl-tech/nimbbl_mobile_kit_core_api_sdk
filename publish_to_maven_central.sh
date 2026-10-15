#!/bin/bash

# Nimbbl Core API SDK - Maven Central Publishing Script
#
# Publishes tech.nimbbl:core-api-sdk to Maven Central via the Sonatype Central Portal.
# Credentials and signing config come from .env (see .env.example) — the Gradle build
# already loads .env itself (see build.gradle.kts), so this script never passes secrets
# as -P flags (those would be visible in `ps`/shell history).
#
# Modes:
#   --dry-run   (default) Build, sign, and install to ~/.m2 only. No network upload.
#   --publish   Upload to the real Sonatype Central Portal. For --release-type release,
#               this is IRREVERSIBLE once Maven Central accepts it — versions can't be
#               deleted or overwritten. Requires interactive confirmation unless --yes.
#
# Release types:
#   release    (default) Publishes version.properties's SDK_VERSION as-is.
#   snapshot   Publishes "<SDK_VERSION>-SNAPSHOT" to the Central Portal's snapshot repo,
#              without touching version.properties (which has no snapshot support).
#              Snapshots can be re-uploaded freely — use this to rehearse the real upload
#              path over the network before doing an actual release.
#
# For --release-type release --publish: the working tree must be fully committed AND pushed
# (hard requirement, not skippable with --yes — see check_git_committed_and_pushed below) so
# the released artifact always corresponds to an exact, shareable commit. On success, that
# commit is tagged "vX.Y.Z" and the tag is pushed, mirroring publish_to_jitpack.sh's convention.
#
# --verify-staging: rehearses a REAL release build + sign + upload + finalize over the network
# (release-type release only) WITHOUT ever actually publishing to Maven Central. It finalizes
# with publishing_type=user_managed instead of automatic, which lands the deployment in the
# Central Portal as a pending, reviewable staging repository instead of auto-releasing it, then
# looks it up and drops it via the OSSRH Staging API's drop endpoint. No git check, no tag.
# Use this to prove the whole pipeline (incl. finalize) works before ever doing a real release.

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info()    { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error()   { echo -e "${RED}❌ $1${NC}"; }

GROUP_ID="tech.nimbbl"
GROUP_PATH=$(echo "$GROUP_ID" | tr '.' '/')
ARTIFACT_ID="core-api-sdk"
VERSION_FILE="version.properties"
ENV_FILE=".env"

MODE="dry-run"
RELEASE_TYPE="release"
TEST_SCOPE="unit"       # none | unit | all
ASSUME_YES=false
BUMP_TYPE=""            # "" | patch | minor | major
VERIFY_STAGING=false

show_help() {
    echo "Nimbbl Core API SDK - Maven Central Publishing Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  --dry-run                      Build, sign, install to ~/.m2 only (default)"
    echo "  --publish                      Upload to the real Sonatype Central Portal"
    echo "  --release-type <release|snapshot>"
    echo "                                 release (default): publish SDK_VERSION as-is, immutable."
    echo "                                 Requires a fully committed AND pushed working tree"
    echo "                                 (hard requirement, not skippable with --yes); on"
    echo "                                 success, tags the released commit vX.Y.Z and pushes it."
    echo "                                 snapshot: publish SDK_VERSION-SNAPSHOT, re-uploadable —"
    echo "                                 use to rehearse --publish for real before a release."
    echo "  --bump <patch|minor|major>     Bump version.properties first, via version_manager.sh"
    echo "                                 (major resets minor+patch to 0, minor resets patch to 0)."
    echo "                                 Required if the current SDK_VERSION is already published."
    echo "  --tests <none|unit|all>        Pre-publish test scope (default: unit)."
    echo "                                 unit = testDebugUnitTest (no device needed)."
    echo "                                 all  = also connectedDebugAndroidTest (needs a device/emulator)."
    echo "  --yes                          Skip the interactive confirmation before a real --publish"
    echo "  --verify-staging               Real network upload + finalize of a release build, but"
    echo "                                 finalized as user_managed (not automatic) and then"
    echo "                                 DROPPED before it can publish. Proves the whole pipeline"
    echo "                                 (incl. finalize) works without ever going live. No git"
    echo "                                 check, no tag. Requires --release-type release (default)."
    echo "  --help                         Show this help message"
    echo
    echo "Examples:"
    echo "  $0                                          # Dry run, unit tests, release type"
    echo "  $0 --publish --verify-staging                # Rehearse release upload+finalize, then drop it (safe)"
    echo "  $0 --bump patch --publish                    # Bump patch, then real RELEASE upload (irreversible)"
    echo "  $0 --release-type snapshot --publish         # Real snapshot upload (safe rehearsal)"
    echo "  $0 --publish                                 # Real RELEASE upload (irreversible, asks to confirm)"
    echo "  $0 --bump patch --publish --yes --tests all  # Bump, real release, full test suite, no prompt (CI use)"
    echo
    local current_ver
    current_ver=$(get_current_version)
    echo "Current version.properties SDK_VERSION: $current_ver"
}

get_current_version() {
    if [ -f "$VERSION_FILE" ]; then
        grep "^SDK_VERSION=" "$VERSION_FILE" | cut -d'=' -f2
    else
        echo "unknown"
    fi
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run) MODE="dry-run"; shift ;;
        --publish) MODE="publish"; shift ;;
        --release-type)
            RELEASE_TYPE="$2"
            if [[ "$RELEASE_TYPE" != "release" && "$RELEASE_TYPE" != "snapshot" ]]; then
                print_error "Invalid --release-type: $RELEASE_TYPE (expected 'release' or 'snapshot')"
                exit 1
            fi
            shift 2
            ;;
        --tests)
            TEST_SCOPE="$2"
            if [[ "$TEST_SCOPE" != "none" && "$TEST_SCOPE" != "unit" && "$TEST_SCOPE" != "all" ]]; then
                print_error "Invalid --tests: $TEST_SCOPE (expected 'none', 'unit', or 'all')"
                exit 1
            fi
            shift 2
            ;;
        --bump)
            BUMP_TYPE="$2"
            if [[ "$BUMP_TYPE" != "patch" && "$BUMP_TYPE" != "minor" && "$BUMP_TYPE" != "major" ]]; then
                print_error "Invalid --bump: $BUMP_TYPE (expected 'patch', 'minor', or 'major')"
                exit 1
            fi
            shift 2
            ;;
        --yes) ASSUME_YES=true; shift ;;
        --verify-staging) VERIFY_STAGING=true; MODE="publish"; shift ;;
        --help) show_help; exit 0 ;;
        *) print_error "Unknown option: $1"; show_help; exit 1 ;;
    esac
done

if [ "$VERIFY_STAGING" = true ] && [ "$RELEASE_TYPE" != "release" ]; then
    print_error "--verify-staging only applies to --release-type release (snapshots have no staging/finalize step — use --release-type snapshot --publish to rehearse those)"
    exit 1
fi

if [ -n "$BUMP_TYPE" ]; then
    if [ ! -x "./version_manager.sh" ]; then
        print_error "version_manager.sh not found or not executable"
        exit 1
    fi
    before_version=$(get_current_version)
    print_info "Bumping version ($BUMP_TYPE) via version_manager.sh — currently $before_version..."
    ./version_manager.sh bump "$BUMP_TYPE"
    after_version=$(get_current_version)
    print_success "$VERSION_FILE updated: $before_version → $after_version"
    print_warning "Remember to commit this: git add $VERSION_FILE && git commit -m \"chore(version): bump to $after_version\""
fi

BASE_VERSION=$(get_current_version)
if [ "$BASE_VERSION" = "unknown" ] || [ -z "$BASE_VERSION" ]; then
    print_error "Could not read SDK_VERSION from $VERSION_FILE"
    exit 1
fi
if [[ "$BASE_VERSION" == *-SNAPSHOT ]]; then
    print_error "$VERSION_FILE's SDK_VERSION ($BASE_VERSION) already ends in -SNAPSHOT."
    print_info "Set a plain MAJOR.MINOR.PATCH there (version_manager.sh) — use --release-type snapshot to publish a snapshot instead."
    exit 1
fi

GRADLE_VERSION_ARGS=()
if [ "$RELEASE_TYPE" = "snapshot" ]; then
    PUBLISH_VERSION="${BASE_VERSION}-SNAPSHOT"
    GRADLE_VERSION_ARGS=("-PpublishVersionOverride=${PUBLISH_VERSION}")
else
    PUBLISH_VERSION="$BASE_VERSION"
fi

print_info "Nimbbl Core API SDK — Maven Central publish"
print_info "  Coordinates:  ${GROUP_ID}:${ARTIFACT_ID}:${PUBLISH_VERSION}"
print_info "  Release type: $RELEASE_TYPE"
print_info "  Mode:         $MODE"
print_info "  Tests:        $TEST_SCOPE"
echo

# --- Pre-flight checks ---

if [ ! -f "$ENV_FILE" ]; then
    print_error "$ENV_FILE not found. Copy .env.example to .env and fill in:"
    print_info "  centralPortalUsername, centralPortalPassword"
    print_info "  signing.keyId, signing.password, signing.secretKeyRingFile"
    exit 1
fi

# Verify required keys are present and non-empty without ever printing their values.
missing_keys=()
for key in centralPortalUsername centralPortalPassword signing.keyId signing.password signing.secretKeyRingFile; do
    value=$(grep "^${key}=" "$ENV_FILE" 2>/dev/null | cut -d'=' -f2-)
    if [ -z "$value" ]; then
        missing_keys+=("$key")
    fi
done
if [ ${#missing_keys[@]} -gt 0 ]; then
    print_error "Missing values in $ENV_FILE for:"
    for key in "${missing_keys[@]}"; do
        print_error "  - $key"
    done
    exit 1
fi
print_success "$ENV_FILE has all required credentials set"

# Confirm the signing key ring file actually exists and isn't empty (a silently-empty
# export, e.g. from a cancelled gpg passphrase prompt, fails the build in a confusing way).
keyring_path=$(grep "^signing.secretKeyRingFile=" "$ENV_FILE" | cut -d'=' -f2-)
if [ ! -s "$keyring_path" ]; then
    print_error "signing.secretKeyRingFile ($keyring_path) does not exist or is empty."
    print_info "Export it with: gpg --export-secret-keys <keyId> > signing-key.gpg"
    exit 1
fi
print_success "Signing key ring file found: $keyring_path"

# A real release upload is immutable and should correspond to an exact, shareable commit —
# so for --release-type release --publish, "committed and pushed" is a hard requirement, not
# a skippable warning (--yes does not bypass this; it only skips the interactive confirmation
# later on). Dry runs and snapshot publishes stay lenient, since they're meant for iterating.
check_git_committed_and_pushed() {
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not a git repository — cannot verify commit/push state for a release publish"
        exit 1
    fi

    if ! git diff-index --quiet HEAD -- 2>/dev/null; then
        print_error "There are uncommitted changes to tracked files. Commit everything before a release publish:"
        git status --short
        exit 1
    fi

    local untracked
    untracked=$(git ls-files --others --exclude-standard)
    if [ -n "$untracked" ]; then
        print_warning "There are untracked files (not part of any commit):"
        echo "$untracked" | sed 's/^/    /'
    fi

    local upstream
    if ! upstream=$(git rev-parse --abbrev-ref --symbolic-full-name '@{u}' 2>/dev/null); then
        print_error "Current branch has no upstream tracking branch. Push it first:"
        print_info "  git push -u origin $(git rev-parse --abbrev-ref HEAD)"
        exit 1
    fi

    if git fetch --quiet origin 2>/dev/null; then
        local ahead
        ahead=$(git rev-list --count '@{u}..HEAD' 2>/dev/null || echo "0")
        if [ "$ahead" != "0" ]; then
            print_error "Local branch has $ahead unpushed commit(s) ahead of $upstream:"
            git log --oneline '@{u}..HEAD'
            print_info "Push before a release publish: git push"
            exit 1
        fi
    else
        print_warning "Could not fetch from origin to verify push status (offline?)."
        print_warning "Proceeding on local information only — this does NOT guarantee $upstream is actually up to date."
    fi

    print_success "Working tree is clean and fully pushed to $upstream"
}

if [ "$MODE" = "publish" ] && [ "$RELEASE_TYPE" = "release" ] && [ "$VERIFY_STAGING" != true ]; then
    check_git_committed_and_pushed
elif [ "$VERIFY_STAGING" = true ]; then
    print_info "--verify-staging: skipping the commit/push and tagging requirements (nothing will actually be released)."
elif git rev-parse --git-dir > /dev/null 2>&1 && ! git diff-index --quiet HEAD -- 2>/dev/null; then
    print_warning "There are uncommitted changes in this repo (fine for $MODE/$RELEASE_TYPE, required for a real release publish)."
fi

# Best-effort check for whether this version is already on Maven Central — never block on
# network failure, since outbound access isn't guaranteed in every environment. Skipped for
# snapshots: search.maven.org doesn't index the snapshot repository the same way.
if [ "$RELEASE_TYPE" = "release" ] && command -v curl &> /dev/null; then
    print_info "Checking whether ${GROUP_ID}:${ARTIFACT_ID}:${PUBLISH_VERSION} is already on Maven Central..."
    search_url="https://search.maven.org/solrsearch/select?q=g:${GROUP_ID}+AND+a:${ARTIFACT_ID}+AND+v:${PUBLISH_VERSION}&rows=1&wt=json"
    response=$(curl -s --max-time 8 "$search_url" 2>/dev/null || echo "")
    if echo "$response" | grep -q '"numFound":0'; then
        print_success "Version $PUBLISH_VERSION is not yet on Maven Central"
    elif echo "$response" | grep -q '"numFound"'; then
        print_error "Version $PUBLISH_VERSION appears to ALREADY be on Maven Central."
        print_info "Maven Central release versions are immutable — bump version.properties (version_manager.sh) first."
        if [ "$MODE" = "publish" ] && [ "$VERIFY_STAGING" != true ]; then
            exit 1
        fi
    else
        print_warning "Could not reach search.maven.org to check — proceeding without this check"
    fi
fi

# --- Pre-publish tests ---

if [ "$TEST_SCOPE" = "unit" ] || [ "$TEST_SCOPE" = "all" ]; then
    print_info "Running unit tests (./gradlew testDebugUnitTest)..."
    ./gradlew testDebugUnitTest
    print_success "Unit tests passed"
fi

if [ "$TEST_SCOPE" = "all" ]; then
    print_info "Running instrumented tests (./gradlew connectedDebugAndroidTest)..."
    ./gradlew connectedDebugAndroidTest
    print_success "Instrumented tests passed"
fi

if [ "$TEST_SCOPE" = "none" ]; then
    print_warning "Skipping tests (--tests none) — not recommended before a real publish"
fi

# --- Build + sign (always local first, regardless of --dry-run/--publish) ---

print_info "Building, signing, and installing to ~/.m2 (publishToMavenLocal)..."
./gradlew publishToMavenLocal "${GRADLE_VERSION_ARGS[@]}"
print_success "Local publish succeeded"

local_dir=~/.m2/repository/${GROUP_PATH}/${ARTIFACT_ID}/${PUBLISH_VERSION}
local_aar="${local_dir}/${ARTIFACT_ID}-${PUBLISH_VERSION}.aar"
local_sig="${local_aar}.asc"
if [ -f "$local_sig" ] && command -v gpg &> /dev/null; then
    print_info "Verifying signature..."
    if gpg --verify "$local_sig" "$local_aar" 2>&1 | grep -q "Good signature"; then
        print_success "Signature verified"
    else
        print_error "Signature verification failed — aborting before any real upload"
        exit 1
    fi
fi

if [ "$MODE" = "dry-run" ]; then
    echo
    print_success "Dry run complete. Nothing was uploaded."
    print_info "Artifacts installed at: $local_dir/"
    print_info "Run '$0 --publish --release-type $RELEASE_TYPE' when ready to upload for real."
    exit 0
fi

# --- Real publish ---

echo
if [ "$RELEASE_TYPE" = "release" ] && [ "$VERIFY_STAGING" = true ]; then
    print_info "About to upload ${GROUP_ID}:${ARTIFACT_ID}:${PUBLISH_VERSION} to the real Sonatype Central Portal for VERIFICATION ONLY."
    print_info "It will be finalized as user_managed (not automatic) and then dropped — it will not be published."
    if [ "$ASSUME_YES" != true ]; then
        read -p "Type the version number ($PUBLISH_VERSION) to confirm: " -r CONFIRM_VERSION
        if [ "$CONFIRM_VERSION" != "$PUBLISH_VERSION" ]; then
            print_info "Confirmation did not match — verification cancelled"
            exit 0
        fi
    fi
elif [ "$RELEASE_TYPE" = "release" ]; then
    print_warning "About to upload ${GROUP_ID}:${ARTIFACT_ID}:${PUBLISH_VERSION} to the real Sonatype Central Portal."
    print_warning "Maven Central RELEASE artifacts cannot be deleted or overwritten once released."
    if [ "$ASSUME_YES" != true ]; then
        read -p "Type the version number ($PUBLISH_VERSION) to confirm: " -r CONFIRM_VERSION
        if [ "$CONFIRM_VERSION" != "$PUBLISH_VERSION" ]; then
            print_info "Confirmation did not match — publishing cancelled"
            exit 0
        fi
    fi
else
    print_info "About to upload SNAPSHOT ${GROUP_ID}:${ARTIFACT_ID}:${PUBLISH_VERSION} — snapshots are safely re-uploadable."
    if [ "$ASSUME_YES" != true ]; then
        read -p "Continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Publishing cancelled"
            exit 0
        fi
    fi
fi

print_info "Uploading (./gradlew publishReleasePublicationToSonatypeRepository)..."
./gradlew publishReleasePublicationToSonatypeRepository "${GRADLE_VERSION_ARGS[@]}"
print_success "Upload complete"

# A plain Gradle maven-publish upload only lands in the OSSRH Staging API compatibility service —
# it does NOT appear in the Central Portal (and therefore never auto-publishes) until explicitly
# finalized. This must run from the same IP as the upload above, which a single script run
# naturally satisfies. See: https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/
finalize_ossrh_deployment_to_portal() {
    local publishing_type="${1:-automatic}"
    local username password auth_token http_code response_file
    username=$(grep "^centralPortalUsername=" "$ENV_FILE" | cut -d'=' -f2-)
    password=$(grep "^centralPortalPassword=" "$ENV_FILE" | cut -d'=' -f2-)
    auth_token=$(printf "%s:%s" "$username" "$password" | base64)
    response_file=$(mktemp)

    print_info "Finalizing the OSSRH-staged deployment into the Central Portal (publishing_type=${publishing_type})..."
    http_code=$(curl -s -o "$response_file" -w "%{http_code}" \
        --request POST \
        --header "Authorization: Bearer ${auth_token}" \
        "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${GROUP_ID}?publishing_type=${publishing_type}")

    if [[ "$http_code" =~ ^2 ]]; then
        print_success "Deployment finalized (HTTP $http_code)"
        [ -s "$response_file" ] && cat "$response_file"
        FINALIZED=true
    else
        print_warning "Could not finalize the deployment (HTTP $http_code)."
        [ -s "$response_file" ] && print_info "Response: $(cat "$response_file")"
        FINALIZED=false
    fi
    rm -f "$response_file"
}

# Used only by --verify-staging. After a finalize with publishing_type=user_managed, the
# deployment sits in the Central Portal as a pending staging repository (never auto-published).
# This looks it up via the search endpoint and drops it, proving the pipeline works end to end
# without anything ever reaching repo1.maven.org.
verify_and_drop_staging_deployment() {
    local username password auth_token response_file repo_count repo_key drop_code
    username=$(grep "^centralPortalUsername=" "$ENV_FILE" | cut -d'=' -f2-)
    password=$(grep "^centralPortalPassword=" "$ENV_FILE" | cut -d'=' -f2-)
    auth_token=$(printf "%s:%s" "$username" "$password" | base64)
    response_file=$(mktemp)

    print_info "Looking up the open staging repository for ${GROUP_ID}..."
    curl -s -o "$response_file" \
        --header "Authorization: Bearer ${auth_token}" \
        "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?state=open&ip=any"

    if ! command -v python3 &> /dev/null; then
        print_warning "python3 not found — can't parse the search response automatically."
        print_info "Raw response: $(cat "$response_file")"
        print_info "Drop it manually: curl -X DELETE --header \"Authorization: Bearer <token>\" https://ossrh-staging-api.central.sonatype.com/manual/drop/repository/<key>"
        rm -f "$response_file"
        return
    fi

    repo_count=$(python3 -c "import json; d=json.load(open('$response_file')); print(len(d.get('repositories', [])))" 2>/dev/null || echo "0")

    if [ "$repo_count" = "0" ]; then
        print_warning "No open staging repositories found. Nothing to verify or drop — check manually at:"
        print_info "  https://central.sonatype.com/publishing/deployments"
        rm -f "$response_file"
        return
    fi

    print_info "Open staging repositories:"
    python3 -c "
import json
d = json.load(open('$response_file'))
for r in d.get('repositories', []):
    print(f\"  key={r.get('key')} state={r.get('state')} portal_deployment_id={r.get('portal_deployment_id')}\")
"

    if [ "$repo_count" != "1" ]; then
        print_warning "More than one open staging repository — not auto-dropping (ambiguous, could affect someone else's in-flight deployment)."
        print_info "Drop the right one manually: curl -X DELETE --header \"Authorization: Bearer <token>\" https://ossrh-staging-api.central.sonatype.com/manual/drop/repository/<key>"
        rm -f "$response_file"
        return
    fi

    repo_key=$(python3 -c "import json; d=json.load(open('$response_file')); print(d['repositories'][0]['key'])")
    print_success "Found the staged (not published) deployment: $repo_key"
    print_info "This proves the real build+sign+upload+finalize pipeline works. Dropping it now..."

    drop_code=$(curl -s -o /dev/null -w "%{http_code}" \
        --request DELETE \
        --header "Authorization: Bearer ${auth_token}" \
        "https://ossrh-staging-api.central.sonatype.com/manual/drop/repository/${repo_key}")

    if [[ "$drop_code" =~ ^2 ]]; then
        print_success "Staging repository dropped (HTTP $drop_code) — verification complete, nothing was published to Maven Central."
    else
        print_warning "Drop request returned HTTP $drop_code — verify/clean up manually at https://central.sonatype.com/publishing/deployments"
    fi
    rm -f "$response_file"
}

echo
if [ "$RELEASE_TYPE" = "release" ] && [ "$VERIFY_STAGING" = true ]; then
    FINALIZED=false
    finalize_ossrh_deployment_to_portal "user_managed"
    if [ "$FINALIZED" = true ]; then
        verify_and_drop_staging_deployment
    else
        print_warning "Finalize failed, so there's nothing staged to verify/drop."
    fi
    echo
    print_success "Verify-staging run complete — no tag created, no git changes, nothing published to Maven Central."
elif [ "$RELEASE_TYPE" = "release" ]; then
    FINALIZED=false
    finalize_ossrh_deployment_to_portal "automatic"
    if [ "$FINALIZED" = true ]; then
        print_info "If validation passes, it will automatically appear at:"
        print_info "  https://repo1.maven.org/maven2/${GROUP_PATH}/${ARTIFACT_ID}/${PUBLISH_VERSION}/"
        print_info "(this can take anywhere from a few minutes to a few hours)"
    else
        print_warning "Finalize it manually instead — log in and check:"
        print_info "  https://central.sonatype.com/publishing/deployments"
    fi

    # Tag the exact commit that was just released, and push it — mirrors publish_to_jitpack.sh's
    # tagging convention (vMAJOR.MINOR.PATCH). check_git_committed_and_pushed already guaranteed
    # a clean, pushed tree above, so HEAD here is exactly what Central Portal just received.
    RELEASE_TAG="v${PUBLISH_VERSION}"
    echo
    if git tag -l | grep -q "^${RELEASE_TAG}$"; then
        print_warning "Tag $RELEASE_TAG already exists locally — not re-creating it"
    else
        print_info "Tagging the released commit as $RELEASE_TAG..."
        if git tag "$RELEASE_TAG"; then
            print_success "Tag $RELEASE_TAG created"
        else
            print_error "Failed to create tag $RELEASE_TAG — create it manually: git tag $RELEASE_TAG"
            RELEASE_TAG=""
        fi
    fi
    if [ -n "$RELEASE_TAG" ]; then
        print_info "Pushing tag $RELEASE_TAG to origin..."
        if git push origin "$RELEASE_TAG"; then
            print_success "Tag $RELEASE_TAG pushed to origin"
        else
            print_error "Upload to Maven Central succeeded, but pushing the tag failed."
            print_info "Push it manually: git push origin $RELEASE_TAG"
        fi
    fi
else
    print_info "Snapshot published to:"
    print_info "  https://central.sonatype.com/repository/maven-snapshots/${GROUP_PATH}/${ARTIFACT_ID}/${PUBLISH_VERSION}/"
fi
