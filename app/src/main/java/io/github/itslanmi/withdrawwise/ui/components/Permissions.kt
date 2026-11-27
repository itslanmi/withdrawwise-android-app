package io.github.itslanmi.withdrawwise.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.ui.theme.extended

val LocationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

fun Context.hasLocationPermission(): Boolean =
    LocationPermissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

fun Context.hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
    PackageManager.PERMISSION_GRANTED

/** Re-checks a permission every time the screen resumes, e.g. after returning from system settings. */
@Composable
fun rememberPermissionState(check: Context.() -> Boolean): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.check()) }
    LifecycleResumeEffect(Unit) {
        granted = context.check()
        onPauseOrDispose { }
    }
    return granted
}

@Composable
fun PermissionCard(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    granted: Boolean,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    WwCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                icon = icon,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            if (granted) {
                Icon(
                    painterResource(R.drawable.ic_check_circle_filled),
                    contentDescription = stringResource(R.string.permission_granted),
                    tint = MaterialTheme.extended.feeFree
                )
            } else {
                PillButton(text = stringResource(R.string.permission_allow), onClick = onRequest, compact = true)
            }
        }
    }
}
