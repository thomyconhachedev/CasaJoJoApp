plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.casajojoapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.casajojoapp"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // BASE_URL por defecto del backend (EC2 de produccion/pruebas).
        // En modo API se puede sobreescribir en runtime desde el panel Admin.
        // Para probar en emulador, cambiar a "http://10.0.2.2:8080/".
        // Debe terminar en "/".
        buildConfigField("String", "BASE_URL", "\"http://100.62.186.171:8080/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Retrofit (Cliente HTTP)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Converter Gson para Retrofit (Convierte automáticamente JSON <-> Data Classes)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Librería Gson nativa
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Iconos Material (AdminPanelSettings, LocationOn); versión la fija el BOM de Compose.
    implementation("androidx.compose.material:material-icons-extended")
}