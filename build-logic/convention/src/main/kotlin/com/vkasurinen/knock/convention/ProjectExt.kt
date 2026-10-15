package com.vkasurinen.knock.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import java.net.URI
import java.util.Properties

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private const val DEFAULT_BASE_URL = "http://localhost:8080"

/**
 * The backend's address without a trailing slash, from BASE_URL in local.properties.
 * The real backend's address stays out of the repo, so without it the local backend is used.
 */
val Project.backendBaseUrl: String
    get() = localProperties().getProperty("BASE_URL")
        ?.trim()
        ?.trimEnd('/')
        ?.takeIf { it.isNotEmpty() }
        ?: DEFAULT_BASE_URL

val Project.backendBaseUri: URI
    get() = URI(backendBaseUrl)

// Replaces AGP's internal gradleLocalProperties(), which isn't public API
private fun Project.localProperties(): Properties {
    val file = rootProject.layout.projectDirectory.file("local.properties")
    return Properties().apply {
        providers.fileContents(file).asText.orNull?.let { load(it.reader()) }
    }
}
