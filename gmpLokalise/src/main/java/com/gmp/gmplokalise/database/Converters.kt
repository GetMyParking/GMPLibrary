package com.gmp.gmplokalise.database

import androidx.room.TypeConverter
import com.gmp.gmplokalise.model.TranslationEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    @TypeConverter
    @JvmStatic
    fun arrayListToString(list: ArrayList<TranslationEntity>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToArrayList(value: String): ArrayList<TranslationEntity> {
        val listType = object : TypeToken<ArrayList<TranslationEntity>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    @JvmStatic
    fun arrayListToString1(list: ArrayList<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringToArrayList1(value: String): ArrayList<String> {
        val listType = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}