import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("maven-publish")
    kotlin("jvm") version "1.9.0"
}


tasks.register<Wrapper>("wrapper") {
    gradleVersion = "8.11.1"
}
tasks.register("prepareKotlinBuildScriptModel"){}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies{

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.json:json:20231013")
}
publishing {
    val sdkArtifactId = "nimbbl-checkout-core-sdk-java"
    val sdkGroupId = "tech.nimbbl.sdk"
    val gitlabToken = "glpat-zswGgsyUM5yVbo9yy6RG"
    val sdkVersion = "3.0.6"
    val mavenRepo = "https://gitlab.com/api/v4/projects/25847308/packages/maven"

    publications {
        publications {
            create<MavenPublication>("release") {
                from(components["java"])
            }
        }
/*        create<MavenPublication>("jarArchive") {
            groupId = sdkGroupId
            artifactId = sdkArtifactId
            version = sdkVersion

            // Specify the JAR artifact
            from(components["java"])

            // Generate valid POM
            pom {
                name.set(sdkArtifactId)
                description.set("Nimbbl Checkout Core SDK for Java")
                url.set("https://your-repository-url.com") // Set a valid project URL
            }
        }*/
    }

    repositories {
        maven {
            url = uri(mavenRepo)
            if (!mavenRepo.startsWith("file")) {
                credentials(HttpHeaderCredentials::class) {
                    name = "Private-Token"
                    value = gitlabToken
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}