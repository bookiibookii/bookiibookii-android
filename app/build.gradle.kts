import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties

val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.bookiibookii.bookiibookii"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bookiibookii.bookiibookii_d"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "ALADIN_TTB_KEY",
            "\"${properties["ALADIN_TTB_KEY"] ?: ""}\""
        )

        buildConfigField("String", "KAKAO_APP_KEY", "\"${properties["kakao_native_app_key"]}\"")
        manifestPlaceholders["KAKAO_APP_KEY"] = properties["kakao_native_app_key"] as String

        buildConfigField("String", "KAKAO_REST_API_KEY", "\"${properties["KAKAO_REST_API_KEY"] ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = properties["RELEASE_STORE_FILE"] as String?
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = properties["RELEASE_STORE_PASSWORD"] as String?
                keyAlias = properties["RELEASE_KEY_ALIAS"] as String?
                keyPassword = properties["RELEASE_KEY_PASSWORD"] as String?
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // KTX
    implementation("androidx.activity:activity-ktx:1.9.0")

    //API 연결 간 의존성 추가
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor")

    //viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // chip
    implementation("com.google.android.material:material:1.13.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    implementation("com.vanniktech:android-image-cropper:4.5.0")

    //안드로이드 표준 SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // 2. OkHttp (통신 로그 확인용)
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")

    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("com.kakao.sdk:v2-all:2.20.1")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // EncryptedSharedPreferences (토큰 암호화 저장)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
}