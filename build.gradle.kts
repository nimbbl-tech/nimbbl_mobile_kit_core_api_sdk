import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library") version "8.10.1"
    kotlin("android") version "1.9.0"
    id("maven-publish")
}

android {
    namespace = "tech.nimbbl.coreapisdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // Event Logging Configuration - Now dynamically determined based on BASE_URL
        // Removed BuildConfig fields as they are now computed at runtime
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Debug build uses gradle.properties values
        }
        
        release {
            isMinifyEnabled = false
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
    gradleVersion = "8.11.1"
}


/*tasks.register("prepareKotlinBuildScriptModel") {}*/

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.auth0.android:jwtdecode:2.0.0")
    
    // JAXB dependencies for JDK 17 compatibility
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.1")
    implementation("javax.activation:activation:1.1.1")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.1")
}

// Optional: include sources in the published artifact
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                // Include sources jar
                artifact(sourcesJar)

                groupId = "com.github.nimbbl-tech"
                artifactId = "nimbbl-checkout-core-sdk"
                version = "3.0.8"

                pom {
                    name.set("nimbbl-checkout-core-sdk")
                    description.set("Nimbbl Checkout Core SDK for Android")
                    url.set("https://github.com/nimbbl-tech/nimbbl-mobile-kit-core-api-sdk")
                    
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("nimbbl-tech")
                            name.set("Nimbbl Team")
                            email.set("team@nimbbl.tech")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://github.com/nimbbl-tech/nimbbl-mobile-kit-core-api-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com/nimbbl-tech/nimbbl-mobile-kit-core-api-sdk.git")
                        url.set("https://github.com/nimbbl-tech/nimbbl-mobile-kit-core-api-sdk")
                    }
                }
            }
        }
    }
}


