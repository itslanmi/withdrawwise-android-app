package io.github.itslanmi.withdrawwise

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.itslanmi.withdrawwise.domain.model.ThemeMode
import io.github.itslanmi.withdrawwise.feature.onboarding.OnboardingScreen
import io.github.itslanmi.withdrawwise.ui.WithdrawWiseApp
import io.github.itslanmi.withdrawwise.ui.navigation.TopLevelDestination
import io.github.itslanmi.withdrawwise.ui.theme.WithdrawWiseTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    /** Tab requested by a notification tap, consumed once the app shell has navigated to it. */
    private var requestedTab by mutableStateOf<TopLevelDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        keepSplashUntilReady()
        setContent {
            val state by appViewModel.state.collectAsStateWithLifecycle()
            val ready = state as? AppUiState.Ready
            val darkTheme =
                when (ready?.themeMode) {
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                    else -> isSystemInDarkTheme()
                }

            // Keep status/navigation bar icons readable when the in-app theme differs from the system one.
            DisposableEffect(darkTheme) {
                val style =
                    if (darkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }

            WithdrawWiseTheme(darkTheme = darkTheme) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    if (ready != null) {
                        AnimatedContent(
                            targetState = ready.onboardingComplete,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "root"
                        ) { onboarded ->
                            if (onboarded) {
                                WithdrawWiseApp(requestedTab = requestedTab, onRequestedTabShow = {
                                    requestedTab = null
                                })
                            } else {
                                OnboardingScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    /** Holds the system splash screen until the profile is loaded, so the first frame is never blank. */
    private fun keepSplashUntilReady() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    val ready = appViewModel.state.value is AppUiState.Ready
                    if (ready) content.viewTreeObserver.removeOnPreDrawListener(this)
                    return ready
                }
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getStringExtra(EXTRA_OPEN_TAB) == TAB_INSIGHTS) requestedTab = TopLevelDestination.Insights
    }

    companion object {
        const val EXTRA_OPEN_TAB = "open_tab"
        const val TAB_INSIGHTS = "insights"
    }
}
