import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version "1.9.0"
}

group = "com.github.tools-nimbbl"
version = "3.0.6-SNAPSHOT-3"

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
            groupId = "com.github.tools-nimbbl" // replace with your GitHub username
            artifactId = "nimbbl_mobile_kit_core_api_sdk"           // replace with your repo/module name
            version = "3.0.6-SNAPSHOT-3"                // update for each build
        }
    }
}
