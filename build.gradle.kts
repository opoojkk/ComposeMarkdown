plugins {
    kotlin("multiplatform") version "2.0.21"
    kotlin("plugin.compose") version "2.0.21"
    id("com.android.application") version "8.5.2"
    id("org.jetbrains.compose") version "1.7.3"
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation("org.jetbrains:markdown:0.7.3")
                implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            }
        }
        val commonTest by getting
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
    }
}

compose.desktop {
    application {
        mainClass = "com.composemarkdown.MainKt"
    }
}

android {
    namespace = "com.composemarkdown"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.composemarkdown"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
