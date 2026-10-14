@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package com.vkasurinen.knock.convention

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

// withAndroidTarget() only matches the legacy androidTarget(). The Android target created by
// AGP 9's com.android.kotlin.multiplatform.library plugin is matched by its platform type.
private fun KotlinHierarchyBuilder.withAndroidLibraryTarget() {
    withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
}

private val hierarchyTemplate = KotlinHierarchyTemplate {
    withSourceSetTree(
        KotlinSourceSetTree.main,
        KotlinSourceSetTree.test,
    )

    common {
        withCompilations { true }

        group("mobile") {
            withAndroidLibraryTarget()
            group("ios") {
                withIos()
            }
        }

        group("jvmCommon") {
            withAndroidLibraryTarget()
            withJvm()
        }

        group("native") {
            withNative()

            group("apple") {
                withApple()

                group("ios") {
                    withIos()
                }

                group("macos") {
                    withMacos()
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.applyHierarchyTemplate() {
    applyHierarchyTemplate(hierarchyTemplate)
}