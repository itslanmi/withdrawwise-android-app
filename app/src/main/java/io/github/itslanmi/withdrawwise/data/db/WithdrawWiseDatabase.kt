package io.github.itslanmi.withdrawwise.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import io.github.itslanmi.withdrawwise.domain.model.Money
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "withdrawals", indices = [Index("timestamp")])
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val atmId: String,
    val atmName: String,
    val atmBankId: String,
    val amountCents: Long,
    val feeCents: Long,
    val timestamp: Long,
    val freeAlternativeMeters: Int?
)

fun WithdrawalEntity.toDomain() = Withdrawal(
    id = id,
    atmId = atmId,
    atmName = atmName,
    atmBankId = atmBankId,
    amount = Money(amountCents),
    fee = Money(feeCents),
    timestamp = Instant.ofEpochMilli(timestamp),
    freeAlternativeMeters = freeAlternativeMeters
)

fun Withdrawal.toEntity() = WithdrawalEntity(
    id = id,
    atmId = atmId,
    atmName = atmName,
    atmBankId = atmBankId,
    amountCents = amount.cents,
    feeCents = fee.cents,
    timestamp = timestamp.toEpochMilli(),
    freeAlternativeMeters = freeAlternativeMeters
)

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun get(id: Long): WithdrawalEntity?

    @Insert
    suspend fun insert(entity: WithdrawalEntity): Long

    @Update
    suspend fun update(entity: WithdrawalEntity)

    @Query("DELETE FROM withdrawals WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM withdrawals")
    suspend fun deleteAll()
}

@Database(entities = [WithdrawalEntity::class], version = 1, exportSchema = true)
abstract class WithdrawWiseDatabase : RoomDatabase() {
    abstract fun withdrawalDao(): WithdrawalDao
}
