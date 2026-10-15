import com.android.build.api.dsl.ApplicationExtension
import com.vkasurinen.knock.convention.backendBaseUri
import com.vkasurinen.knock.convention.configureKotlinAndroid
import com.vkasurinen.knock.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                namespace = "com.vkasurinen.knock"

                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                    versionName = libs.findVersion("projectVersionName").get().toString()

                    // Email links of the backend set in local.properties
                    manifestPlaceholders["backendScheme"] = backendBaseUri.scheme
                    manifestPlaceholders["backendHost"] = backendBaseUri.host
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        // Lets the release variant be run locally to judge real performance.
                        // Replace with a proper release key before publishing.
                        signingConfig = signingConfigs.getByName("debug")
                    }
                }

                configureKotlinAndroid(this)
            }
        }
    }
}