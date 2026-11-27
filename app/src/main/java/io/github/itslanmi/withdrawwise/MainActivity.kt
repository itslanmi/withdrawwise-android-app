package io.github.itslanmi.withdrawwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.itslanmi.withdrawwise.ui.WithdrawWiseApp
import io.github.itslanmi.withdrawwise.ui.theme.WithdrawWiseTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WithdrawWiseTheme {
                WithdrawWiseApp()
            }
        }
    }
}
