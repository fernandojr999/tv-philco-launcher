plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.casa.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.casa.launcher"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }

    kotlinOptions { jvmTarget = "17" }
}
