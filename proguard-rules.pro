# ===========================================
# Nimbbl Core API SDK - Internal ProGuard Rules
# These rules are used when building the SDK itself
# ===========================================

# ===========================================
# Public API Protection (Keep Readable)
# ===========================================
# Keep all public classes and methods that merchants use
-keep public class tech.nimbbl.coreapisdk.core.NimbblCoreApiSDK { *; }

# Keep public methods in public classes
-keepclassmembers public class tech.nimbbl.coreapisdk.** {
    public <methods>;
}

# Keep companion objects
-keep class tech.nimbbl.coreapisdk.**$Companion { *; }

# Keep Kotlin extension functions
-keep class tech.nimbbl.coreapisdk.**Kt { *; }

# Keep utility classes used by other SDKs
-keep class tech.nimbbl.coreapisdk.utils.logging.EventLoggingUtils { *; }

# RestApiUtils - used by WebView SDK for URL configuration
-keep class tech.nimbbl.coreapisdk.api.RestApiUtils { *; }

# ApiResult - public API returned by updateOrder(); must not be repackaged/obfuscated so AAR consumers can resolve .code, .rawBody, .isSuccessful
-keep class tech.nimbbl.coreapisdk.api.ApiResult { *; }

# ===========================================
# JSON / Model classes (Critical)
# ===========================================
# Keep model classes used by JsonParser (org.json) - fields and constructors
-keep class tech.nimbbl.coreapisdk.api.models.** {
    <fields>;
    <init>(...);
    <methods>;
}
-keep class tech.nimbbl.coreapisdk.data.models.** {
    <fields>;
    <init>(...);
    <methods>;
}
-keep class tech.nimbbl.coreapisdk.utils.payloads.** { *; }

# Keep constants classes used by other SDKs
-keep class tech.nimbbl.coreapisdk.core.constants.EventConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.PayloadKeys { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.ServiceConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.ServiceConstants$Companion { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.Constants { *; }

# Keep service interfaces (Kotlin interfaces, no Retrofit)
-keep interface tech.nimbbl.coreapisdk.api.services.** { *; }

# ===========================================
# Obfuscation Configuration
# ===========================================
# Don't repackage model classes - JsonParser and reflection use them
-keep class tech.nimbbl.coreapisdk.api.models.** { *; }
-keep class tech.nimbbl.coreapisdk.data.models.** { *; }

# Repackage only non-model classes to avoid conflicts
-repackageclasses 'tech.nimbbl.coreapisdk.obfuscated'

# ===========================================
# Kotlin / Coroutines
# ===========================================
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Disable string concatenation optimization (not available on Android)
-dontoptimize

# Suppress warnings
-dontwarn java.lang.invoke.StringConcatFactory

# ===========================================
# Debugging Support
# ===========================================
# Keep line numbers for better error messages
-keepattributes SourceFile,LineNumberTable

# Keep generic signatures for reflection
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*