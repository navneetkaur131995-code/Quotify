plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.quotify.core"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    // Android Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Kotlin Coroutines
    implementation(libs.kotlin.coroutines)

    // Retrofit + OKHttp
    implementation(libs.squareup.retrofit)
    implementation(platform(libs.squareup.okhttp.bom))
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.gson.converter)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)

    // Hilt
    implementation(libs.android.hilt)
    ksp(libs.android.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}