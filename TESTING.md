# Running Tests

## Unit tests (JVM, no device needed)

```bash
# All unit tests
./gradlew testDebugUnitTest

# Just the DataMasker PII/token redaction tests
./gradlew testDebugUnitTest --tests 'tech.nimbbl.coreapisdk.utils.DataMaskerTest'
```

[`DataMaskerTest`](src/test/java/tech/nimbbl/coreapisdk/utils/DataMaskerTest.kt) covers
`DataMasker.sanitizeForLogging`: masking of auth tokens (`token`/`access_token`/`accessToken`,
`Bearer ...`) and user PII (`email`, `mobile_number`/`mobile`/`phone`, `otp`, name and address
fields), plus the empty-value and null/blank-input edge cases.

## Instrumented tests (requires a connected device or running emulator)

Most of this module's tests live under `src/androidTest`, because `JsonParser` and
`ProcessOutputPayloads` need real `org.json` (unavailable in plain JVM tests here — no
Robolectric), and `NimbblCheckoutSDK`/`CoreAppWebService`/`HttpConnectionHelper` need a real
Android runtime.

```bash
# All instrumented tests
./gradlew connectedDebugAndroidTest

# A single test class, e.g.:
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=tech.nimbbl.coreapisdk.utils.JsonParserTest
```

Check a device/emulator is attached first with `adb devices`.

Key suites:
- [`JsonParserTest`](src/androidTest/java/tech/nimbbl/coreapisdk/utils/JsonParserTest.kt) — every
  response type `JsonParser.fromJson` dispatches on (`OrderResponse`, `CheckoutResourceVo`,
  `ResolveUserResponse`, `InitiatePaymentResponse`, `PublicKeyResponse`, `BinDataResponse`,
  `UpdateTransactionResponse`, `TransactionEnquiryResponseVo`, `ResendOtpResponse`,
  `ListOfBankResponse`, `ListOfWalletResponse`, `PaymentModesResponse`), keyed by exact JSON field
  names (some differ from the Kotlin property name, e.g. `PayloadKeys.key_user_name` literally
  equals `"key_user_name"` — check `PayloadKeys.kt` before trusting a key name here).
- [`ProcessOutputPayloadsTest`](src/androidTest/java/tech/nimbbl/coreapisdk/utils/payloads/ProcessOutputPayloadsTest.kt)
  — every JS-bridge output-payload builder, including the "literal string `\"null\"` as last name"
  regression guard in `getResolveUserOutputPayload`.
- [`NimbblRepositoryImplTest`](src/androidTest/java/tech/nimbbl/coreapisdk/data/repository/NimbblRepositoryImplTest.kt)
  — mocks `CoreAppWebService` with Mockito to verify request-building and `RawApiResponse` →
  `ApiResult<T>` mapping for every repository method, with no real network call.
- [`HttpConnectionHelperTest`](src/androidTest/java/tech/nimbbl/coreapisdk/api/HttpConnectionHelperTest.kt)
  and [`CoreAppWebServiceTest`](src/androidTest/java/tech/nimbbl/coreapisdk/api/services/CoreAppWebServiceTest.kt)
  — real `HttpURLConnection` round-trips against [`LocalHttpServer`](src/androidTest/java/tech/nimbbl/coreapisdk/testutil/LocalHttpServer.kt),
  a minimal loopback `ServerSocket`-based fake server (see "Mocking the HTTP layer" below for why).
- [`NimbblCoreApiSDKTest`](src/androidTest/java/tech/nimbbl/coreapisdk/core/NimbblCoreApiSDKTest.kt)
  and [`NimbblCoreApiSDKRepositoryTest`](src/androidTest/java/tech/nimbbl/coreapisdk/core/NimbblCoreApiSDKRepositoryTest.kt)
  — the latter points the global `ServiceConstants.BASE_URL` at `LocalHttpServer` to exercise the
  facade's lazy `getAPIRepositoryInstance()` init path for real. Resets the companion-level
  `nimbblApiRepository` singleton via reflection in `setUp`/`tearDown()` (there's no production
  reset API) so it doesn't leak state into/from other test classes that also trigger it.
- [`EventLoggingServiceTest`](src/androidTest/java/tech/nimbbl/coreapisdk/utils/logging/EventLoggingServiceTest.kt)
  — uses `EventLoggingService.eventLogUrlOverride` (a `@VisibleForTesting` var added specifically
  for this; production code never sets it) to redirect the real event-send network call at
  `LocalHttpServer` instead of the real `eventlogpipe.nimbbl.tech`. `logEvent()` fires a
  non-blocking coroutine with no handle to await — `LocalHttpServer.takeRequest(timeout)` absorbs
  that by blocking until the post arrives, rather than needing a sleep.
- extension/logging-config suites — see inline doc comments per file.

### Mocking the HTTP layer

`HttpConnectionHelper` is a Kotlin `object` wrapping `java.net.HttpURLConnection`. Mocking it
(e.g. via `Mockito.mockStatic`) needs Mockito's **inline** mock maker, which doesn't work on
Android's ART runtime (it crashes with `ClassNotFoundException: java.lang.management.ManagementFactory`
— confirmed empirically, not a config guess). Two different workarounds are used here, matched to
what's actually being tested:

- **`NimbblRepositoryImpl`**: mocked at the `CoreAppWebService` boundary instead (a plain class,
  not an `object`). Android's default (non-inline) Mockito mock maker can mock it via subclassing,
  but only because `CoreAppWebService` and its methods are marked `open` — Kotlin classes/methods
  are `final` by default, which the non-inline maker can't override. This is the only reason that
  class is `open`; it's otherwise a purely internal (already-obfuscated-in-release) class.
- **`CoreAppWebService`/`HttpConnectionHelper` themselves**: tested against `LocalHttpServer`, a
  ~100-line hand-rolled HTTP/1.1 server over a real loopback `ServerSocket` — no mocking, no new
  runtime dependency, just a real (local, offline, fast) socket round-trip.

Dependencies added for this: `org.mockito:mockito-android` (not `mockito-core` — see above),
`org.mockito.kotlin:mockito-kotlin`, `org.jetbrains.kotlinx:kotlinx-coroutines-test`.

## Coverage

```bash
# Unit test coverage report (JaCoCo via AGP)
./gradlew createDebugUnitTestCoverageReport
# → build/reports/coverage/test/debug/index.html

# Instrumented test coverage report (requires a connected device/emulator)
./gradlew createDebugAndroidTestCoverageReport
# → build/reports/coverage/androidTest/debug/connected/index.html
```

As of this writing: ~70% instruction / ~78% line coverage (up from 4%/6% before this module had
any real tests). Still untested: `interfaces.checkout` (explicitly unimplemented stubs per this
file's architecture notes — not meant to be tested) and a handful of defensive error branches
that are hard to trigger without more invasive fault injection (e.g. `CoreAppWebService`'s own
outer `catch` blocks, which `HttpConnectionHelper` basically never lets reach since it already
catches everything itself and returns a `code = -1` response instead of throwing).

## Lint

```bash
./gradlew lint
```
