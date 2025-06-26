plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kwamboka.omnibot101"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kwamboka.omnibot101"
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.5.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.biometric:biometric:1.2.0-alpha04")
    implementation ("androidx.compose.material:material-icons-extended:1.6.1")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation ("com.airbnb.android:lottie-compose:6.3.0")
    implementation ("com.airbnb.android:lottie-compose:6.3.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")

    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.3")

    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")

}