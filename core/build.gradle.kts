plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ktlint)
}

ktlint {
    version.set("1.5.0")
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
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
                "proguard-rules.pro",
            )
        }
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
    ktlintRuleset(libs.compose.rules.ktlint)
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

    // Paging
    implementation(libs.androidx.compose.paging)
    implementation(libs.androidx.compose.paging.runtime)

    // Navigation3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
