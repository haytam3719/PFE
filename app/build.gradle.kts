plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") version "4.3.8" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    kotlin("kapt")

}
apply(plugin = "com.google.dagger.hilt.android")
apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.example.project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/LICENSE-notice.md")
            resources.excludes.add("META-INF/gradle/incremental.annotation.processors")
        }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose=true
        viewBinding = true
        dataBinding=true
    }


}


dependencies {
    implementation("io.coil-kt:coil-compose:1.3.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation ("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-database:19.2.0")
    implementation ("com.google.firebase:firebase-firestore-ktx")


    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")

    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    implementation("com.google.firebase:firebase-functions-ktx:20.4.0")

    // Dagger Hilt dependencies

    implementation ("com.google.dagger:hilt-android:2.48")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    //implementation("androidx.camera:camera-core:1.3.2")
    implementation ("androidx.camera:camera-camera2:1.3.2")

    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("androidx.compose.material3:material3-android:1.2.1")
    kapt ("com.google.dagger:hilt-android-compiler:2.48")
    kapt ("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
    implementation ("androidx.fragment:fragment-ktx:1.4.0")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    kapt ("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation ("androidx.biometric:biometric:1.1.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}