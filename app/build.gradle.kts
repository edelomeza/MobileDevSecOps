plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.check)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
}

android {
    val keysFile = rootProject.file("keys.properties")
    if (keysFile.exists()) {
        keysFile.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val eqIndex = trimmed.indexOf('=')
                if (eqIndex > 0) {
                    project.ext.set(
                        trimmed.substring(0, eqIndex).trim(),
                        trimmed.substring(eqIndex + 1).trim(),
                    )
                }
            }
        }
    }

    namespace = "com.example.mobiledevsecops"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mobiledevsecops"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath =
                System.getenv("KEYSTORE_PATH") ?: project.findProperty("keystore.path") as? String
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword =
                    System.getenv("KEYSTORE_STORE_PASSWORD")
                        ?: project.findProperty("keystore.store.password") as? String
                        ?: return@create
                keyAlias =
                    System.getenv("KEYSTORE_KEY_ALIAS")
                        ?: project.findProperty("keystore.key.alias") as? String
                        ?: return@create
                keyPassword =
                    System.getenv("KEYSTORE_KEY_PASSWORD")
                        ?: project.findProperty("keystore.key.password") as? String
                        ?: return@create
            }
        }
    }

    val baseUrl: String = project.findProperty("api.base.url") as? String ?: "https://10.0.2.2:7227"

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/LICENSE-notice.md")
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "com.example.mobiledevsecops.BuildConfig",
                    "com.example.mobiledevsecops.MainActivity",
                    "com.example.mobiledevsecops.MobileDevSecOpsApp",
                    "com.example.mobiledevsecops.di.*",
                    "com.example.mobiledevsecops.ui.theme.*",
                    "com.example.mobiledevsecops.util.*",
                )
            }
        }
        verify {
            rule {
                minBound(0)
            }
        }
    }
}

ktlint {
    debug.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    ignoreFailures.set(false)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.security.crypto)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockk)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

detekt {
    config.setFrom("../detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencyCheck {
    formats = listOf("HTML", "JSON")
    failBuildOnCVSS = 7.0f
    suppressionFile = "../dependency-check-suppressions.xml"
}
