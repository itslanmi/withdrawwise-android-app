package io.github.itslanmi.withdrawwise.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.itslanmi.withdrawwise.data.seed.SeedParser
import io.github.itslanmi.withdrawwise.domain.model.Atm
import io.github.itslanmi.withdrawwise.domain.model.Bank
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Serves banks and ATMs from the JSON files bundled in `assets/seed`. Parsed once, then cached.
 * Swapping this for a network-backed source later only means providing another [DirectoryRepository].
 */
@Singleton
class AssetDirectoryRepository
@Inject
constructor(@param:ApplicationContext private val context: Context) :
    DirectoryRepository {
    private val mutex = Mutex()
    private var banks: List<Bank>? = null
    private var atms: List<Atm>? = null

    override suspend fun banks(): List<Bank> = mutex.withLock {
        banks
            ?: SeedParser.parseBanks(read(BANKS)).also { banks = it }
    }

    override suspend fun atms(): List<Atm> = mutex.withLock {
        atms
            ?: SeedParser.parseAtms(read(ATMS)).also { atms = it }
    }

    private suspend fun read(path: String): String = withContext(Dispatchers.IO) {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }

    private companion object {
        const val BANKS = "seed/banks.json"
        const val ATMS = "seed/atms_ljubljana.json"
    }
}
