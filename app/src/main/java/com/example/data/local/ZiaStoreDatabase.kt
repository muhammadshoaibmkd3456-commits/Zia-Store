package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ExpenseEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity

@Database(
    entities = [
        StoreProfile::class,
        ProductEntity::class,
        PartyEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZiaStoreDatabase : RoomDatabase() {
    abstract fun storeProfileDao(): StoreProfileDao
    abstract fun productDao(): ProductDao
    abstract fun partyDao(): PartyDao
    abstract fun transactionDao(): TransactionDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: ZiaStoreDatabase? = null

        fun getDatabase(context: Context): ZiaStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZiaStoreDatabase::class.java,
                    "ziastore_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
