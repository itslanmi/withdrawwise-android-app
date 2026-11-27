package io.github.itslanmi.withdrawwise.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.itslanmi.withdrawwise.data.AssetDirectoryRepository
import io.github.itslanmi.withdrawwise.data.DataStorePreferencesRepository
import io.github.itslanmi.withdrawwise.data.RoomWithdrawalRepository
import io.github.itslanmi.withdrawwise.data.db.WithdrawWiseDatabase
import io.github.itslanmi.withdrawwise.data.db.WithdrawalDao
import io.github.itslanmi.withdrawwise.domain.repository.DirectoryRepository
import io.github.itslanmi.withdrawwise.domain.repository.PreferencesRepository
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import javax.inject.Singleton

private val Context.userPreferences by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object DataProvidersModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): WithdrawWiseDatabase =
        Room.databaseBuilder(context, WithdrawWiseDatabase::class.java, "withdrawwise.db").build()

    @Provides
    fun withdrawalDao(database: WithdrawWiseDatabase): WithdrawalDao = database.withdrawalDao()

    @Provides
    @Singleton
    fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.userPreferences
}

@Module
@InstallIn(SingletonComponent::class)
interface DataBindingsModule {
    @Binds
    fun directory(impl: AssetDirectoryRepository): DirectoryRepository

    @Binds
    fun withdrawals(impl: RoomWithdrawalRepository): WithdrawalRepository

    @Binds
    fun preferences(impl: DataStorePreferencesRepository): PreferencesRepository
}
