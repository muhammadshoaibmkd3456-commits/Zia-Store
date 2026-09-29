package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isArchived = 0 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findAnyByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stockQty <= 0 AND isArchived = 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQty > 0 AND stockQty <= lowStockThreshold AND isArchived = 0 ORDER BY stockQty ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE expiryDate IS NOT NULL AND expiryDate <= :thresholdTime AND isArchived = 0 ORDER BY expiryDate ASC")
    fun getNearExpiryProducts(thresholdTime: Long): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products WHERE isArchived = 0 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :id")
    suspend fun archiveProduct(id: Long)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQty = stockQty + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Double)

    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0")
    suspend fun getProductCount(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
