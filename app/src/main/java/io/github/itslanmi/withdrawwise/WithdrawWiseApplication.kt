package io.github.itslanmi.withdrawwise

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.github.itslanmi.withdrawwise.notification.SummaryScheduler
import javax.inject.Inject

@HiltAndroidApp
class WithdrawWiseApplication :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var summaryScheduler: SummaryScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        summaryScheduler.schedule()
    }
}
