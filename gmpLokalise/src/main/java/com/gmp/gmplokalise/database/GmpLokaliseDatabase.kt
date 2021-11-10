package com.gmp.gmplokalise.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gmp.gmplokalise.model.LanguageEntity
import com.gmp.gmplokalise.model.TranslationEntity

@Database(entities = [TranslationEntity::class,LanguageEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class GmpLokaliseDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao

    companion object {
        private var instance: GmpLokaliseDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): GmpLokaliseDatabase? {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, GmpLokaliseDatabase::class.java,
                    "GmpLokaliseSDK.db"
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