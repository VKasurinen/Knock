package com.vkasurinen.knock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun DeepLinkListener(
    navController: NavController,
    deepLinks: Flow<String> = emptyFlow()
) {
    // Deep links for this window only. ExternalUriHandler has one listener for the whole app,
    // which isn't enough when the desktop app has several windows.
    LaunchedEffect(navController, deepLinks) {
        deepLinks.collect { uri ->
            navController.navigate(NavUri(uri))
        }
    }

    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            navController.navigate(NavUri(uri))
        }

        onDispose {
            ExternalUriHandler.listener = null
        }
    }
}