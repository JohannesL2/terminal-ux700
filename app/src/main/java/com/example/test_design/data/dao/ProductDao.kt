package com.example.test_design.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test_design.data.entity.ProductEntity
import com.example.test_design.data.utils.generateArticleNumber
import com.example.test_design.data.utils.generateEAN
import com.example.test_design.data.utils.generateRowNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM productentity")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM productentity")
    suspend fun getAllProductsOnce(): List<ProductEntity>

    @Query("SELECT * FROM productentity WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(product: ProductEntity)

    suspend fun insertInitialProducts() {
        val initialProducts = listOf(
            ProductEntity(
                name ="Kaffe",
                description = "Bryggkaffe",
                price = 25,
                category = "Dryck",
                imageResName = "Kaffe",
                ean = generateEAN(),
                articleNumber = generateArticleNumber(),
                rowNumber = generateRowNumber()
                ),
            ProductEntity(
                name = "Latte",
                description =  "Varm mjölk + espresso",
                price = 59,
                category = "Dryck",
                imageResName = "Latte",
                ean = generateEAN(),
                articleNumber = generateArticleNumber(),
                rowNumber = generateRowNumber()
            ),
            ProductEntity(
                name = "Kaka",
                description =  "Kladdkaka",
                price = 39,
                category = "Snacks",
                imageResName = "Kaka",
                ean = generateEAN(),
                articleNumber = generateArticleNumber(),
                rowNumber = generateRowNumber()
            ),
            ProductEntity(
                name = "Smörgås",
                description =  "Ost och skinka",
                price = 55,
                category = "Mat",
                imageResName = "Smörgås",
                ean = generateEAN(),
                articleNumber = generateArticleNumber(),
                rowNumber = generateRowNumber()
            )
        )

        for (p in initialProducts) {
            if (getByName(p.name) == null) {
                insert(p)
            }
        }
    }
}