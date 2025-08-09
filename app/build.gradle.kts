import com.github.megatronking.stringfog.plugin.StringFogExtension
import top.niunaijun.blackobfuscator.BlackObfuscatorExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("stringfog")
//    id("top.niunaijun.blackobfuscator")
}

//apply(plugin = "stringfog")
//apply(plugin = "top.niunaijun.blackobfuscator")
//
//configure<StringFogExtension> {
//    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
//    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
//}
//configure<BlackObfuscatorExtension> {
//    enabled = true
//    depth = 2
//    setObfClass("com.ly")
//}

android {
    signingConfigs {
        create("lynb") {
            storeFile = file("E:\\ly\\key\\android\\lynb.jks")
            storePassword = "#@L522nb666324Y@"
            keyAlias = "lynb"
            keyPassword = "#@L522nb666324Y@"
        }
    }
    namespace = "com.ly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ly"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("lynb")
        externalNativeBuild {
            cmake {
                cppFlags += ""
                abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("lynb")
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("lynb")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    buildToolsVersion = "36.0.0"
    ndkVersion = "25.1.8937393"
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("com.github.megatronking.stringfog:xor:5.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
}