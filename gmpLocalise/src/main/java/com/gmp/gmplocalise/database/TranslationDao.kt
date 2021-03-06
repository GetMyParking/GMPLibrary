package com.gmp.gmplocalise.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gmp.gmplocalise.model.LanguageEntity
import com.gmp.gmplocalise.model.TranslationEntity

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translationEntity: ArrayList<TranslationEntity>)

    @Query("select value from translations where `key`=:key and iso=:iso COLLATE NOCASE")
    suspend fun getString(key: String, iso: String): List<String>

    @Query("DELETE from translations")
    suspend fun removeAll()

    @Query(" UPDATE `sqlite_sequence` SET `seq` = 0 WHERE `name` = 'translations'")
    suspend fun updateSequence()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIso(translationEntity: ArrayList<LanguageEntity>)

    @Query("select * from language ")
    suspend fun getAllLanguages(): List<String>

}