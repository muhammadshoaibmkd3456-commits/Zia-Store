package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties WHERE type = :type ORDER BY name ASC")
    fun getPartiesByType(type: PartyType): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    fun getPartyByIdFlow(id: Long): Flow<PartyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>)

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Delete
    suspend fun deleteParty(party: PartyEntity)

    @Query("UPDATE parties SET currentBalance = currentBalance + :delta WHERE id = :partyId")
    suspend fun adjustPartyBalance(partyId: Long, delta: Double)

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun getPartyCount(): Int

    @Query("DELETE FROM parties")
    suspend fun deleteAll()
}
