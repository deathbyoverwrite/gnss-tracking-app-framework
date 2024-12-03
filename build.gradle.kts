// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // Disable compose compiler project wise when working with Kotlin 2.0
    alias(libs.plugins.compose.compiler) apply false
}
