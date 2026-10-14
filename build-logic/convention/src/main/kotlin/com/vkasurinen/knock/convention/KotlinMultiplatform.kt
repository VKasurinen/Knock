package com.vkasurinen.knock.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKotlinMultiplatform() {
    configureAndroidLibraryTarget()
    configureDesktopTarget()

    extensions.configure<KotlinMultiplatformExtension> {
        // iosX64 (Intel simulators) is no longer a supported target in Kotlin 2.4
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = this@configureKotlinMultiplatform.pathToFrameworkName()
            }
        }

        applyHierarchyTemplate()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }
}

// AGP 9: the Android target of a KMP library comes from the
// com.android.kotlin.multiplatform.library plugin and is configured through
// `kotlin { android { ... } }` instead of the old `android { ... }` (LibraryExtension) block.
private fun Project.configureAndroidLibraryTarget() {
    extensions.configure<KotlinMultiplatformExtension> {
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
            namespace = this@configureAndroidLibraryTarget.pathToPackageName()
            compileSdk = libs.findVersion("projectCompileSdkVersion").get().toString().toInt()
            minSdk = libs.findVersion("projectMinSdkVersion").get().toString().toInt()

            // Required for Compose Multiplatform resources on Android
            androidResources.enable = true

            enableCoreLibraryDesugaring = true

            // Runs commonTest on the JVM: ./gradlew testAndroidHostTest
            withHostTest { }

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
