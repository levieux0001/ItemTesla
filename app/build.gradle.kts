// build.gradle.kts (Module: app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Essencial para o Room
    id("org.jetbrains.dokka") version "1.9.10" // Dokka
}

android {
    namespace = "com.example.itemtesla"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.itemtesla"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Componentes do Room Database (Verifique a versão, deve ser a mesma para runtime, compiler e ktx)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1") // Para corrotinas com Room

    // Corrotinas (Kotlin Coroutines) - Embora room-ktx já inclua, é bom ter certeza ou adicionar para outros usos
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") // Versão estável
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // Para uso em Android UI

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}