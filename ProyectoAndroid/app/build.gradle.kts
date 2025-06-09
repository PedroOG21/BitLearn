plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.proyectoandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectoandroid"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding{
        enable = true
    }
}

dependencies {

    //maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("androidx.fragment:fragment-ktx:1.5.7")
    implementation(libs.androidx.swiperefreshlayout)


    implementation("com.google.android.gms:play-services-auth:21.2.0")

    //AÑADIMOS ESTAS 3 LIBRERIAS PARA API
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.picasso:picasso:2.8")

    //Activity
    implementation("androidx.activity:activity-ktx:1.10.0")
    //viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    //liveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    implementation ("com.google.firebase:firebase-messaging:23.1.2")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    implementation ("androidx.fragment:fragment-ktx:1.5.7")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}