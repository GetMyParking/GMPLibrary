package com.gmp.gmplocalise.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gmp.gmplocalise.model.LanguageEntity
import com.gmp.gmplocalise.model.TranslationEntity

@Database(entities = [TranslationEntity::class, LanguageEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class GmpLocaliseDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao

    companion object {
        private var instance: GmpLocaliseDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): GmpLocaliseDatabase? {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, GmpLocaliseDatabase::class.java,
                    "GmpLocaliseSDK.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return instance

        }

        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }


    }
}