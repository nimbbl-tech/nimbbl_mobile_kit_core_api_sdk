pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://gitlab.com/api/v4/projects/25847308/packages/maven")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
    plugins {
        id("com.android.library") version "8.6.1" apply false
        kotlin("android") version "1.9.0" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://gitlab.com/api/v4/projects/25847308/packages/maven")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "nimbbl_mobile_kit_core_api_sdk" 