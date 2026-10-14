import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * The shared KMP module that the platform apps (androidApp, desktopApp, iosApp) are built from.
 * Replaces the course's CmpApplicationConventionPlugin, whose Android application part moved
 * to the androidApp module (AGP 9 doesn't allow com.android.application + KMP in one module).
 */
class CmpSharedConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.vkasurinen.convention.cmp.feature")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinNativeTarget>().configureEach {
                    binaries.withType<Framework>().configureEach {
                        isStatic = true
                    }
                }
            }
        }
    }
}
