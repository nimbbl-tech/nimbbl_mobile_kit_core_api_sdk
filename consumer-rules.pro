# ===========================================
# Nimbbl Core API SDK - Consumer ProGuard Rules
# These rules are automatically applied when merchants use minification
# ===========================================

# ===========================================
# JWT Token Parsing (Critical)
# ===========================================
# Auth0 JWT library
-keep class com.auth0.android.jwt.** { *; }
-keep class com.auth0.jwt.** { *; }

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

# Keep constants classes used by other SDKs
-keep class tech.nimbbl.coreapisdk.core.constants.EventConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.PayloadKeys { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.ServiceConstants { *; }
-keep class tech.nimbbl.coreapisdk.core.constants.ServiceConstants$Companion { *; }

# RestApiUtils - used by WebView SDK for URL configuration
-keep class tech.nimbbl.coreapisdk.api.RestApiUtils { *; }

# ApiResult - public API; keep so consumers (e.g. WebView SDK) can use .code, .rawBody, .isSuccessful
-keep class tech.nimbbl.coreapisdk.api.ApiResult { *; }

# ===========================================
# JSON / Model classes (Essential)
# ===========================================
# Keep model classes used by JsonParser (org.json)
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
-dontwarn kotlin.reflect.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**