package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderNumber: String,
    val dateTime: String,
    val total: Int
)