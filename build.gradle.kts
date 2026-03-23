import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

// Load .env into project properties (so findProperty works). .env is gitignored.
val envFile = rootProject.file(".env")
if (envFile.exists()) {
    envFile.reader().use { reader ->
        reader.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val idx = trimmed.indexOf('=')
                if (idx > 0) {
                    val key = trimmed.substring(0, idx).trim()
                    val value = trimmed.substring(idx + 1).trim()
                        .removeSurrounding("\"").removeSurrounding("'")
                    if (!project.hasProperty(key)) {
                        // Make available to project.findProperty(...)
                        rootProject.extensions.extraProperties.set(key, value)
                    }
                }
            }
        }
    }
}

// Load version properties
val versionProperties = Properties()
val versionPropertiesFile = file("version.properties")
if (versionPropertiesFile.exists()) {
    versionProperties.load(versionPropertiesFile.inputStream())
}

plugins {
    id("com.android.library")
    kotlin("android") version "1.9.0"  // LTS Kotlin version for maximum merchant app compatibility
    id("maven-publish")
    id("signing")
}

// Dependency versions - Using stable versions for maximum merchant app compatibility
val gradleWrapperVersion = "8.2"
val coreKtxVersion = "1.12.0"
val appCompatVersion = "1.6.1"
val coroutinesVersion = "1.7.3"
val jwtDecodeVersion = "2.0.0"
val junitVersion = "4.13.2"
val junitExtVersion = "1.1.5"
val espressoVersion = "3.5.1"

// No OkHttp/Retrofit/Gson - uses java.net.HttpURLConnection and org.json (built-in)

android {
    namespace = "tech.nimbbl.coreapisdk"
    compileSdk = versionProperties.getProperty("COMPILE_ANDROID_SDK", "34").toInt()

    defaultConfig {
        minSdk = versionProperties.getProperty("MIN_ANDROID_SDK", "21").toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // Build config fields for version information
        buildConfigField("String", "SDK_VERSION", "\"${versionProperties.getProperty("SDK_VERSION", "1.0.0")}\"")
        buildConfigField("String", "BUILD_DATE", "\"${versionProperties.getProperty("BUILD_DATE", "unknown")}\"")
        buildConfigField("String", "BUILD_TYPE", "\"${versionProperties.getProperty("BUILD_TYPE", "debug")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Debug build uses gradle.properties values
        }
        
        release {
            isMinifyEnabled = true  // Enable SDK obfuscation
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release build uses gradle.properties values
        }
        
        // Staging build uses gradle.properties values
        create("staging") {
            initWith(getByName("release"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.register<Wrapper>("nimbbl_coreapisdk_wrapper") {
    gradleVersion = gradleWrapperVersion
}


/*tasks.register("prepareKotlinBuildScriptModel") {}*/

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.auth0.android:jwtdecode:$jwtDecodeVersion")

    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$junitExtVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
}

// Maven Central coordinates (tech.nimbbl namespace)
val PUBLISH_GROUP_ID = "tech.nimbbl"
val PUBLISH_ARTIFACT_ID_CORE = "core-api-sdk"
val PUBLISH_VERSION = versionProperties.getProperty("SDK_VERSION", "1.0.0")

group = PUBLISH_GROUP_ID
version = PUBLISH_VERSION

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = PUBLISH_GROUP_ID
                artifactId = PUBLISH_ARTIFACT_ID_CORE
                version = PUBLISH_VERSION

                pom {
                    name.set("Nimbbl Core API SDK")
                    description.set("Nimbbl Checkout Core API SDK for Android - payments, order and transaction APIs.")
                    url.set("https://github.com/nimbbl-tech/nimbbl_mobile_kit_core_api_sdk")
                    packaging = "aar"

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("nimbbl-tech")
                            name.set("Bigital Technologies Pvt. Ltd")
                            email.set("tech@nimbbl.biz")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/nimbbl-tech/nimbbl_mobile_kit_core_api_sdk.git")
                        developerConnection.set("scm:git:ssh://github.com/nimbbl-tech/nimbbl_mobile_kit_core_api_sdk.git")
                        url.set("https://github.com/nimbbl-tech/nimbbl_mobile_kit_core_api_sdk")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "sonatype"
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = project.findProperty("centralPortalUsername") as String?
                        ?: System.getenv("CENTRAL_PORTAL_USERNAME")
                        ?: project.findProperty("ossrhTokenUsername") as String?
                        ?: System.getenv("OSSRH_TOKEN_USERNAME")
                        ?: ""
                    password = project.findProperty("centralPortalPassword") as String?
                        ?: System.getenv("CENTRAL_PORTAL_PASSWORD")
                        ?: project.findProperty("ossrhTokenSecret") as String?
                        ?: System.getenv("OSSRH_TOKEN_SECRET")
                        ?: ""
                }
            }
        }
    }
    signing {
        val signingKeyId: String? = project.findProperty("signing.keyId") as String?
        val signingPassword: String? = project.findProperty("signing.password") as String?
        val signingKey: String? = project.findProperty("signing.secretKeyRingFile") as String?
        if (signingKeyId != null && signingPassword != null && signingKey != null) {
            sign(publishing.publications["release"])
        }
    }
}
