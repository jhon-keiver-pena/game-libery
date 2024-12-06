plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gamelibery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gamelibery"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Agregar dependencias de OkHttp y Gson
    implementation(libs.okhttp)
    implementation(libs.gson)

    implementation(libs.glide) // Versión más reciente al momento
    annotationProcessor(libs.compiler) // Para usar el compilador de Glide
    implementation(libs.okhttp3.integration)



}