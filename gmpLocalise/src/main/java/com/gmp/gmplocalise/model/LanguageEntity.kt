package com.gmp.gmplocalise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language")
data class LanguageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "name")
    val iso: String,
)