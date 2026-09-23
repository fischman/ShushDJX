plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.fischman.shushdjx"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.fischman.shushdjx"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "0.3"
    }

    signingConfigs {
        create("debugRelease") { // Re-use debug keystore for release APK. YOLO.
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("upload") {
            storeFile = file(System.getProperty("user.home") + "/.android/upload-keystore.jks")
            keyAlias = "upload"
            storePassword = "yoyoyo" // The JKS itself is the secret; only non-empty key b/c keytool refuses empty passwords.
            keyPassword = "yoyoyo" // The JKS itself is the secret; only non-empty key b/c keytool refuses empty passwords.
        }
    }

    buildTypes {
        getByName("debug") {
            // Debug uses these settings by default anyway.
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("upload")
        }

        create("debugRelease") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debugRelease")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
}
