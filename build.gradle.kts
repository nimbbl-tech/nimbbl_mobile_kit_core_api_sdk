plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies{

    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.json:json:20240303")
}