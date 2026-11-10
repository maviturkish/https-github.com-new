plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// GitHub Actions run number -> version, so each new build installs as an update
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.maviturkish.pomodoro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maviturkish.pomodoro"
        minSdk = 24
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
    }

    signingConfigs {
        create("release") {
            // Personal-use key kept in the repo so every build can update the previous install
            storeFile = file("pomodoro.keystore")
            storePassword = "pomodoro123"
            keyAlias = "pomodoro"
            keyPassword = "pomodoro123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}
