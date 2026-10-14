import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hot.reload)
    alias(libs.plugins.conveyor)
}

version = "1.0.0"

// FFmpeg binary for the OS the build runs on, so voice messages work with ./gradlew run
val ffmpegForCurrentOs = run {
    val os = System.getProperty("os.name").lowercase()
    val isArm = System.getProperty("os.arch").lowercase().let { it == "aarch64" || it == "arm64" }
    when {
        os.contains("win") -> libs.jave.nativebin.win64
        os.contains("mac") -> if(isArm) libs.jave.nativebin.osxm1 else libs.jave.nativebin.osx64
        else -> if(isArm) libs.jave.nativebin.linux.arm64 else libs.jave.nativebin.linux64
    }
}

// jfa, the macOS helper of jSystemThemeDetector, asks for JNA's "jpms" jar. KNotify needs a newer
// JNA that has no "jpms" jar, so jfa gets the plain JNA jar instead. A metadata rule, unlike a
// substitution, also applies to the copied configurations of the Conveyor plugin.
dependencies.components.withModule("de.jangassen:jfa") {
    allVariants {
        withDependencies {
            removeAll { it.group == "net.java.dev.jna" && it.name == "jna" }
            add("net.java.dev.jna:jna:5.7.0")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.domain)
    implementation(projects.core.presentation)
    implementation(projects.core.designsystem)
    implementation(projects.feature.chat.data)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlin.stdlib)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.jsystemthemedetector)
    implementation(libs.jetbrains.compose.components.resources)
    implementation(libs.composenativetray)
    implementation(libs.knotify)

    // Conveyor packages: each OS only gets its own Compose (Skia) native library.
    // compose.desktop.currentOs above covers run and hot reload.
    linuxAmd64(compose.desktop.linux_x64)
    linuxAarch64(compose.desktop.linux_arm64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)

    // run, hot reload and the Compose packages of the current OS
    implementation(ffmpegForCurrentOs)
    // Conveyor packages for the other platforms. There's no FFmpeg build for Windows on ARM.
    linuxAmd64(libs.jave.nativebin.linux64)
    linuxAarch64(libs.jave.nativebin.linux.arm64)
    macAmd64(libs.jave.nativebin.osx64)
    macAarch64(libs.jave.nativebin.osxm1)
    windowsAmd64(libs.jave.nativebin.win64)
}

compose.desktop {
    application {
        mainClass = "com.vkasurinen.knock.MainKt"

        nativeDistributions {
            packageName = "com.vkasurinen.knock"

            macOS {
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSMicrophoneUsageDescription</key>
                        <string>Knock uses the microphone to record voice messages.</string>
                    """.trimIndent()
                }
            }
        }
    }
}
