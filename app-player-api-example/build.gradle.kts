plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.9.0"
}
apply(rootProject.file("gradle-config/constants.gradle"))

android {
    namespace = "com.bytedance.vodplayer.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bytedance.vodplayer.example"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.coil-kt:coil:2.5.0")

    val bytedanceSDK = project.extra["bytedance"] as Map<String, String>
    implementation("com.bytedanceapi:ttsdk-player_premium:${bytedanceSDK["ttsdkVersion"]}")
    implementation("com.bytedanceapi:ttvideoengine-debugtool:${bytedanceSDK["debugToolVersion"]}")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}