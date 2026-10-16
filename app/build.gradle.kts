plugins {
    id("com.android.application")
}

android {
    namespace = "id.wemoment.canonmonitor"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.wemoment.canonmonitor"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // UVC library fork published to Maven Central. Uses the classic Serenegiant API.
    implementation("org.uvccamera:lib:0.1.0")
}
