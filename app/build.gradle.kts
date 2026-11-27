import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// The Google Maps key is read from local.properties (MAPS_API_KEY=...) or from the
// MAPS_API_KEY environment variable, so it never ends up in version control.
val mapsApiKey: String =
    Properties()
        .apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use(::load)
        }.getProperty("MAPS_API_KEY")
        ?: System.getenv("MAPS_API_KEY")
        ?: ""

android {
    namespace = "io.github.itslanmi.withdrawwise"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.itslanmi.withdrawwise"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.gson)
    implementation(libs.vico.views)

    testImplementation(libs.junit)
}
