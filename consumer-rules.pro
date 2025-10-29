# ===========================================
# Nimbbl Core API SDK - Consumer ProGuard Rules
# These rules are automatically applied when merchants use minification
# ===========================================

# ===========================================
# JWT Token Parsing (Critical)
# ===========================================
# JWT Library
-keep class com.auth0.android.jwt.** { *; }
-keep class com.auth0.jwt.** { *; }

# Gson TypeToken for JWT parsing
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>(...);
}

# JWT parsing methods
-keep class tech.nimbbl.coreapisdk.utils.extensions.NimbblSDKExtensions {
    public static java.lang.String parseJwtToken(android.content.Context, java.lang.String);
    public static java.lang.String getMerchantIDFromJwtToken(android.content.Context, java.lang.String);
}

# ===========================================
# Nimbbl Core API SDK Classes (Essential)
# ===========================================
# Keep public SDK classes
-keep public class tech.nimbbl.coreapisdk.core.NimbblCoreApiSDK { *; }
-keep public class tech.nimbbl.coreapisdk.utils.payloads.OrderCreationPayload { *; }
-keep public class tech.nimbbl.coreapisdk.api.models.requests.CreateOrderRequest { *; }
-keep public class tech.nimbbl.coreapisdk.api.models.responses.CreateOrderResponse { *; }

# Keep constants classes used by other SDKs
-keep class tech.nimbbl.coreapisdk.core.constants.EventConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.PayloadKeys { *; }

# ===========================================
# JSON Serialization (Essential)
# ===========================================
# Keep model classes for JSON serialization - CRITICAL for Gson
# Keep classes, constructors, fields, and methods
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

# Keep JSON annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===========================================
# Essential Attributes
# ===========================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ===========================================
# Suppress Warnings
# ===========================================
-dontwarn com.google.gson.**
-dontwarn com.google.gson.reflect.**
-dontwarn kotlin.reflect.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**