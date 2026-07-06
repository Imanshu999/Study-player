// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    
    // KSP प्लगइन के एरर को यहाँ वर्ज़न फिक्स करके ठीक किया गया है
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
    
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false
}
