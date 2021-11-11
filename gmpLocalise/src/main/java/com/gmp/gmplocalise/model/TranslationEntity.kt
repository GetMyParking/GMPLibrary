package com.gmp.gmplocalise.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
 class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int=0,
    @ColumnInfo(name = "key")

    val key: String,
    @ColumnInfo(name = "iso")

    val iso: String,
    @ColumnInfo(name = "value")

    val value: String
)