import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version "1.9.0"
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "8.11.1"
}

/*tasks.register("prepareKotlinBuildScriptModel") {}*/

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.json:json:20231013")
}

// ✅ Publishing setup for JitPack
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.nimbbl-tech" // replace with your GitHub username
            artifactId = "nimbbl-checkout-core-sdk-java"           // replace with your repo/module name
            version = "3.0.6"
            pom {
                name.set("nimbbl-checkout-core-sdk-java")
                description.set("Nimbbl Checkout CORE SDK JAVA")
            }// update for each build
        }
    }
}

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
}
