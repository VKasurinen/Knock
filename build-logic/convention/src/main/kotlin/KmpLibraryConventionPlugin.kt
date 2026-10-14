import com.vkasurinen.knock.convention.configureKotlinMultiplatform
import com.vkasurinen.knock.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // AGP 9: com.android.library can no longer be combined with Kotlin Multiplatform
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureKotlinMultiplatform()

            dependencies {
                "coreLibraryDesugaring"(libs.findLibrary("android-desugarJdkLibs").get())
                "commonMainImplementation"(libs.findLibrary("kotlinx-serialization-json").get())
                "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
                "commonTestImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
                "commonTestImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
