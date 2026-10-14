package com.vkasurinen.knock

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.vkasurinen.knock.core.presentation.locale.AppLocaleEnvironment
import com.vkasurinen.knock.auth.presentation.navigation.AuthGraphRoutes
import com.vkasurinen.knock.chat.presentation.navigation.ChatGraphRoutes
import com.vkasurinen.knock.navigation.DeepLinkListener
import com.vkasurinen.knock.navigation.NavigationRoot
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.compose.viewmodel.koinViewModel


@Composable
@Preview
fun App(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    deepLinks: Flow<String> = emptyFlow(),
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCheckingAuth) {
        if(!state.isCheckingAuth) {
            onAuthenticationChecked()
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is MainEvent.OnSessionExpired -> {
                navController.navigate(AuthGraphRoutes.Graph) {
                    popUpTo(AuthGraphRoutes.Graph) {
                        inclusive = false
                    }
                }
            }
        }
    }

    KnockTheme(
        darkTheme = isDarkTheme
    ) {
        if(!state.isCheckingAuth) {
            // The NavController lives outside, so the back stack survives a language switch
            AppLocaleEnvironment(language = state.appLanguage) {
                NavigationRoot(
                    navController = navController,
                    startDestination = if(state.isLoggedIn) {
                        ChatGraphRoutes.Graph
                    } else {
                        AuthGraphRoutes.Graph
                    }
                )
            }
            DeepLinkListener(
                navController = navController,
                deepLinks = deepLinks
            )
        }
    }
}