plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.appleader707.syncrecorder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.appleader707.syncrecorder"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    //==================== Modules ====================
    implementation(project(":common"))

    //==================== Compiler ====================
    implementation(libs.androidx.compiler)

    //==================== Core ====================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    //==================== Compose Libraries ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    // Material design icons
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)

    //==================== Lifecycle ====================
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //==================== Ui Material 3 ====================
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //==================== Hilt ====================
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)   // Hilt compiler
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //==================== Timber ====================
    implementation(libs.timber)

    //==================== Accompanist ====================
    implementation(libs.accompanist.permissions)

    //==================== Datastore ====================
    implementation(libs.androidx.datastore.preferences)

    //==================== CameraX ====================
    implementation(libs.androidx.camera.core)

    //==================== FFmpeg ====================
    implementation(libs.ffmpeg.kit.full.gpl)

    //==================== Gson ====================
    implementation(libs.gson)

    //==================== Exoplayer ====================
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    //==================== Charts For Compose ====================
    implementation(libs.mpandroidchart)
}