package io.github.itslanmi.withdrawwise.data

import io.github.itslanmi.withdrawwise.data.db.WithdrawalDao
import io.github.itslanmi.withdrawwise.data.db.toDomain
import io.github.itslanmi.withdrawwise.data.db.toEntity
import io.github.itslanmi.withdrawwise.domain.model.Withdrawal
import io.github.itslanmi.withdrawwise.domain.repository.WithdrawalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomWithdrawalRepository
@Inject
constructor(private val dao: WithdrawalDao) : WithdrawalRepository {
    override fun observeAll(): Flow<List<Withdrawal>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun get(id: Long): Withdrawal? = dao.get(id)?.toDomain()

    override suspend fun add(withdrawal: Withdrawal): Long = dao.insert(withdrawal.copy(id = 0).toEntity())

    override suspend fun update(withdrawal: Withdrawal) = dao.update(withdrawal.toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun deleteAll() = dao.deleteAll()
}
