package com.vkasurinen.knock.deeplink

import java.awt.Desktop
import javax.swing.SwingUtilities

/**
 * Deep links from outside the app: as a program argument when the OS starts the app for a link,
 * or through [Desktop.setOpenURIHandler] while it's running (macOS only).
 */
object DesktopDeepLinkHandler {

    // The same links as the deep links of the nav graphs. The email links of the backend set in
    // local.properties match on the path, so its address doesn't need to be in the code.
    private val supportedUriPatterns = listOf(
        Regex("^knock://.*"),
        Regex("^https?://[^/]+/api/auth/(verify|reset-password)\\?.*"),
    )

    private var isInitialized = false

    fun setup(onDeepLink: (String) -> Unit) {
        if(isInitialized || !Desktop.isDesktopSupported()) {
            return
        }
        isInitialized = true

        try {
            val desktop = Desktop.getDesktop()
            if(desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
                desktop.setOpenURIHandler { event ->
                    val uri = cleanUri(event.uri.toString())
                    if(isSupported(uri)) {
                        SwingUtilities.invokeLater {
                            onDeepLink(uri)
                        }
                    }
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * The first program argument that is a supported deep link.
     */
    fun findDeepLink(args: Array<String>): String? {
        return args
            .map(::cleanUri)
            .firstOrNull(::isSupported)
    }

    // Some launchers pass the link with quotes around it
    private fun cleanUri(uri: String): String = uri.trim('"', ' ')

    private fun isSupported(uri: String): Boolean {
        return supportedUriPatterns.any { it.matches(uri) }
    }
}
