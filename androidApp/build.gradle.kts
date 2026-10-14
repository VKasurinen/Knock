plugins {
    alias(libs.plugins.convention.android.application.compose)
    alias(libs.plugins.google.services)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.feature.chat.database)

    implementation(libs.androidx.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.koin.android)

    implementation(libs.jetbrains.compose.ui.tooling.preview)
}