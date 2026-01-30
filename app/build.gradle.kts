plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "open.vision"
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "open.vision"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        externalNativeBuild {
            cmake {
                arguments += "-DOpenCV_DIR=" + project(":sdk").projectDir + "/native/jni"
                arguments += "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
                arguments += "-DOPENCV_FROM_SDK=TRUE"
                arguments += "-DANDROID_TOOLCHAIN=clang"
                arguments += "-DANDROID_STL=c++_shared"
                targets.add("vision")
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(project(":sdk"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}