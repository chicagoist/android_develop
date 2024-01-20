plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

group = "com.example.myproduct"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

android {
    compileSdkVersion(28)
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(28)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform("com.example.platform:product-platform"))
    testImplementation(platform("com.example.platform:test-platform"))

    implementation(kotlin("stdlib"))
}
