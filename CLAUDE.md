# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

Android library (AAR) — the **Nimbbl Core API SDK**. It handles payment API calls, order/transaction management, event logging, and JWT/device utilities. It is a dependency of the WebView SDK; never import WebView SDK types here.

- Maven coordinates: `tech.nimbbl:core-api-sdk:<version>`
- Package: `tech.nimbbl.coreapisdk`
- Min SDK 21 / Target SDK 34 / Kotlin 1.9.0 / Java 17

## Commands

```bash
# Build
./gradlew clean assembleDebug
./gradlew clean assembleRelease     # enables ProGuard/minification
./gradlew clean assembleStaging     # same as release, for staging deploys

# Test
./gradlew test                       # unit tests
./gradlew connectedAndroidTest       # instrumented tests (requires device/emulator)

# Single test class
./gradlew test --tests "tech.nimbbl.coreapisdk.ExampleUnitTest"

# Lint
./gradlew lint

# Publish to Sonatype (snapshots or staging, based on version suffix)
./gradlew publishReleasePublicationToSonatypeRepository \
  -PossrhTokenUsername=... -PossrhTokenSecret=... \
  -Psigning.keyId=... -Psigning.password=... -Psigning.secretKeyRingFile=...
```

### Version management

```bash
./version_manager.sh --show           # print current version
./version_manager.sh --bump patch     # bump patch in version.properties
./version_manager.sh --bump minor
./version_manager.sh --bump major
```

Version is read from `version.properties` (`SDK_VERSION`). `build.gradle.kts` injects it into `BuildConfig.SDK_VERSION` and the Maven POM. After bumping, commit `version.properties` before tagging.

### Publishing to JitPack

```bash
./publish_to_jitpack.sh --snapshot   # snapshot tag + prints dependency line
./publish_to_jitpack.sh --current    # stable tag from current version.properties
```

Requires `GITHUB_USERNAME=nimbbl-tech` and `REPO_NAME=nimbbl_mobile_kit_core_api_sdk` exported. For Sonatype: append `-SNAPSHOT` to `SDK_VERSION` for snapshot repo; omit for staging.

## Architecture

### Layer order (top → bottom)

```
NimbblCoreApiSDK          ← public singleton facade
NimbblRepository(Impl)    ← data layer; all methods use withContext(Dispatchers.IO)
CoreAppWebService         ← builds headers, calls HttpConnectionHelper, returns RawApiResponse
HttpConnectionHelper      ← raw HttpURLConnection; 30s timeout; returns HttpResponse
```

### Key design constraints

- **No OkHttp, Retrofit, Gson, or Moshi.** Uses `java.net.HttpURLConnection` + `org.json` to avoid transitive dependency conflicts in merchant apps. Do not add these.
- **Manual JSON parsing** in `JsonParser.kt` using `org.json`. When adding new response models, add a parser function here with the existing null-safe extension pattern (`optStringOrNull`, `optIntOrNull`).
- **No DI framework.** `NimbblCoreApiSDK` directly instantiates `NimbblRepositoryImpl`. The dependency graph is hardwired.
- **`INimbblCheckoutBaseSDKInterface`** and its impl are unimplemented stubs — do not build on top of them.

### ApiResult — response wrapper

All repository methods return `ApiResult`. It wraps every HTTP response:

```kotlin
data class ApiResult(
    val isSuccessful: Boolean,
    val code: Int,
    val rawBody: String?,
    val data: JSONObject?   // parsed body, null on error
)
```

When adding a new API call: call `CoreAppWebService`, get back `RawApiResponse`, parse with `JsonParser`, wrap result in `ApiResult`. Follow the existing pattern in `NimbblRepositoryImpl`.

### Environment / URL resolution

`RestApiUtils.kt` resolves environment from the base URL passed to `initialiseAPISDK()`:
- IP-based URL → dev/QA (routes event logs to `qa1eventlogpipe.qa.nimbbl.tech`)
- `api-qa1.nimbbl.tech` → QA
- `api.nimbbl.tech` → production (`sonic.nimbbl.tech` for WebView URLs)

`Constants.kt` reads `EVENT_LOG_URL` and `DEFAULT_TENANT_ID` from `RestApiUtils` at class-load time. The base URL **must** be set before any `Constants` field is accessed.

### Event logging

`EventLoggingService` is a second singleton (independent of the main SDK singleton). It uses a `SupervisorJob` on `Dispatchers.IO` so analytics failures never affect payment flows. All tokens in logs are masked by `DataMasker` (shows first 5 + last 7 chars).

### ProGuard

Two rule files:
- `proguard-rules.pro` — applied to the SDK's own release build. Repackages internal classes to `tech.nimbbl.coreapisdk.obfuscated`; keeps public API and model classes readable.
- `consumer-rules.pro` — automatically applied to merchant apps that consume the AAR. Keeps public classes, model classes, Auth0 JWT, and Gson `TypeToken` (required by Auth0 even though the SDK itself doesn't use Gson directly).

When adding new public classes that merchants or the WebView SDK will reference, add `-keep` rules to **both** files.

### Credentials (.env)

`build.gradle.kts` loads a `.env` file from the repo root (gitignored) into `project.findProperty(...)`. Use this for local publish credentials instead of `~/.gradle/gradle.properties`:

```
centralPortalUsername=...
centralPortalPassword=...
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=...
```

`build.gradle.kts` resolves publish credentials with this fallback order (first non-null wins):
`centralPortalUsername` → `CENTRAL_PORTAL_USERNAME` (env) → `ossrhTokenUsername` → `OSSRH_TOKEN_USERNAME` (env).
The Jenkinsfile passes `-PossrhTokenUsername` / `-PossrhTokenSecret`, which resolves at the third slot.
