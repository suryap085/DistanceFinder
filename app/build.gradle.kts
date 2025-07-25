plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "com.location.distancefinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.location.distancefinder"
        minSdk = 24
        targetSdk = 35
        versionCode = 10
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Ensure 64-bit architecture support
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        
        // Privacy and security
        manifestPlaceholders["appAuthRedirectScheme"] = applicationId
    }

    signingConfigs {
        create("release") {
            // These will be provided via environment variables or keystore
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "release-keystore.jks")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: ""
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: ""
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: ""
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            manifestPlaceholders["enableCrashReporting"] = false
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["enableCrashReporting"] = true
            
            // Bundle optimization
            bundle {
                language {
                    enableSplit = true
                }
                density {
                    enableSplit = true
                }
                abi {
                    enableSplit = true
                }
            }
        }
        
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
    
    // Play Store requirements
    bundle {
        storeArchive {
            enable = false
        }
    }
    
    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core library desugaring for API compatibility
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:6.2.1")
    implementation("com.google.maps.android:maps-compose-utils:6.2.1")
    implementation("com.google.maps.android:maps-compose-widgets:6.2.1")
    
    // Places API
    implementation("com.google.android.libraries.places:places:4.1.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // App Startup
    implementation("androidx.startup:startup-runtime:1.2.0")
    
    // WorkManager (for background tasks)
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    
    // Performance monitoring
    implementation("androidx.tracing:tracing:1.2.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.4.4")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.52")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.work:work-testing:2.9.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.52")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
    useBuildCache = true
}

secrets {
    defaultPropertiesFileName = "secrets.defaults.properties"
    ignoreList.add("keyAlias")
    ignoreList.add("keyPassword")
    ignoreList.add("storeFile")
    ignoreList.add("storePassword")
}