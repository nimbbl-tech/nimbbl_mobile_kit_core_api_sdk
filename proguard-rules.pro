# ===========================================
# Nimbbl Core API SDK - Internal ProGuard Rules
# These rules are used when building the SDK itself
# ===========================================

# ===========================================
# Public API Protection (Keep Readable)
# ===========================================
# Keep all public classes and methods that merchants use
-keep public class tech.nimbbl.coreapisdk.core.NimbblCoreApiSDK { *; }
-keep public class tech.nimbbl.coreapisdk.utils.payloads.OrderCreationPayload { *; }
-keep public class tech.nimbbl.coreapisdk.api.models.requests.CreateOrderRequest { *; }
-keep public class tech.nimbbl.coreapisdk.api.models.responses.CreateOrderResponse { *; }
-keep public class tech.nimbbl.coreapisdk.api.services.OrderCreationService { *; }

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

# ===========================================
# JSON Serialization (Critical)
# ===========================================
# Keep all model classes used for JSON serialization
# Keep classes, constructors, fields, and methods for Gson
-keep class tech.nimbbl.coreapisdk.api.models.** {
    <fields>;
    <init>(...);
    <methods>;
}

# Keep constants classes used by other SDKs
-keep class tech.nimbbl.coreapisdk.core.constants.EventConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.PayloadKeys { *; }

# Keep data model classes used for JSON serialization
-keep class tech.nimbbl.coreapisdk.data.models.** {
    <fields>;
    <init>(...);
    <methods>;
}

# Keep JSON serialization annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===========================================
# Retrofit Services (Critical)
# ===========================================
# Keep Retrofit service interfaces
-keep interface tech.nimbbl.coreapisdk.api.services.** { *; }

# Keep Retrofit annotations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ===========================================
# Obfuscation Configuration
# ===========================================
# Important: Model classes MUST stay in original packages for Gson
# Don't repackage model classes - Gson needs exact class names and packages
-keep class tech.nimbbl.coreapisdk.api.models.** { *; }
-keep class tech.nimbbl.coreapisdk.data.models.** { *; }
-keep class tech.nimbbl.coreapisdk.utils.payloads.** { *; }

# Repackage only non-model classes to unique package to avoid conflicts
-repackageclasses 'tech.nimbbl.coreapisdk.obfuscated'

# ===========================================
# Internal Classes (Can Be Obfuscated)
# ===========================================
# Allow obfuscation of internal implementation classes
# (These will be obfuscated but still functional)

# ===========================================
# Dependencies
# ===========================================
# Keep essential dependency classes
-keep class okhttp3.OkHttpClient { *; }
-keep class retrofit2.Retrofit { *; }
-keep class com.google.gson.Gson { *; }

# Disable string concatenation optimization (not available on Android)
-dontoptimize

# Suppress warnings
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**
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