// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://dl.bintray.com/ppartisan/maven") }
        maven { setUrl("https://clojars.org/repo") }
        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        google()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath ("com.github.megatronking.stringfog:gradle-plugin:5.1.0")
        // 选用加解密算法库，默认实现了xor算法，也可以使用自己的加解密库。
        classpath ("com.github.megatronking.stringfog:xor:5.0.0")
        classpath ("com.android.tools.build:gradle:8.1.3")
        //扁平化加密
        classpath ("com.github.CodingGay:BlackObfuscator-ASPlugin:3.9")
    }
}