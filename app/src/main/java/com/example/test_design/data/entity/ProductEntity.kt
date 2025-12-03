package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProductEntity")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,

    val ean: String,
    val articleNumber: String,
    val rowNumber: Int
)