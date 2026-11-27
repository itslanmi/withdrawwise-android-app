package io.github.itslanmi.withdrawwise.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.itslanmi.withdrawwise.MainActivity
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.domain.SummaryKind
import io.github.itslanmi.withdrawwise.domain.Totals
import io.github.itslanmi.withdrawwise.ui.components.hasNotificationPermission
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Builds and posts the weekly and monthly summary notifications. */
@Singleton
class SummaryNotifier
@Inject
constructor(@param:ApplicationContext private val context: Context) {
    fun canNotify(): Boolean =
        context.hasNotificationPermission() && NotificationManagerCompat.from(context).areNotificationsEnabled()

    @SuppressLint("MissingPermission")
    fun show(kind: SummaryKind, totals: Totals, range: ClosedRange<LocalDate>) {
        if (!canNotify()) return
        ensureChannel()
        val title =
            when (kind) {
                SummaryKind.Weekly -> context.getString(R.string.notification_weekly_title)

                SummaryKind.Monthly -> {
                    val month = range.start.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                    context.getString(
                        R.string.notification_monthly_title,
                        month.replaceFirstChar {
                            it.titlecase(Locale.getDefault())
                        }
                    )
                }
            }
        val body =
            if (totals.fees.isZero) {
                context.resources.getQuantityString(
                    R.plurals.notification_body_free,
                    totals.count,
                    totals.withdrawn.formatShort(),
                    totals.count
                )
            } else {
                context.resources.getQuantityString(
                    R.plurals.notification_body,
                    totals.count,
                    totals.withdrawn.formatShort(),
                    totals.count,
                    totals.fees.format(),
                    totals.freePercent
                )
            }

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_withdrawwise)
                .setColor(ContextCompat.getColor(context, R.color.brand_ember))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(openInsightsIntent())
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .build()
        NotificationManagerCompat.from(context).notify(kind.ordinal + 1, notification)
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notification_channel_description) }
        manager.createNotificationChannel(channel)
    }

    private fun openInsightsIntent(): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_INSIGHTS)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "summaries"
    }
}
