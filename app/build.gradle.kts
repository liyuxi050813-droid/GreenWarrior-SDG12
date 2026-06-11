plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.a207387_liyuxi_ptizwan_lab04"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.a207387_liyuxi_ptizwan_lab04"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.googlefonts)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation & ViewModel (already present)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.7.0-alpha11")
    implementation("androidx.room:room-ktx:2.7.0-alpha11")
    ksp("androidx.room:room-compiler:2.7.0-alpha11")

    // Retrofit + Gson (API 集成)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Lifecycle + Coroutines for API calls
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Google Play Services — Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Firebase BoM + Cloud Sync
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")

    // Coroutines support for Firebase await() on Tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}

apply(plugin = "com.google.gms.google-services")
