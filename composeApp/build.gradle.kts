import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.google.gms.google-services")
    id("com.android.application")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

android {
    namespace = "com.gibson.games"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.gibson.games"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.compose.ui:ui")
       // implementation("androidx.compose.ui:ui-tooling-preview")
       // debugImplementation("androidx.compose.ui:ui-tooling")

        // Material
        //implementation("androidx.compose.material:material")
       // implementation("androidx.compose.material3:material3") // Optional for Material 3

        // Runtime & animation
        //implementation("androidx.compose.runtime:runtime")
       // implementation("androidx.compose.animation:animation")

        // Activity integration
        //implementation("androidx.activity:activity-compose:1.9.0")

        // ViewModel integration
        //implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
        //implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

        // Navigation for Compose
        //implementation("androidx.navigation:navigation-compose:2.7.7")

        // Optional
       // implementation("androidx.compose.runtime:runtime-livedata")
        //implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
       // implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    debugImplementation(compose.uiTooling)
}
