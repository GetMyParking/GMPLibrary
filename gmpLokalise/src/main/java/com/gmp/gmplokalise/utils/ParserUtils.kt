package com.gmp.gmplokalise.utils

import android.content.Context
import com.gmp.gmplokalise.R
import com.gmp.gmplokalise.model.*
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.StringReader
import java.net.URL

class ParserUtils {

    fun parseData(
        url: String
    ): kotlinx.coroutines.flow.Flow<Any?> = flow {
        runCatching {
            try {
                val apiResponse =
                    URL(url).readText()

                val gson = Gson()

                val modelToReturn: TranslationResponse?
                val reader = JsonReader(StringReader(apiResponse))
                reader.isLenient = true
                modelToReturn =
                    gson.fromJson(
                        reader,
                        TranslationResponse::class.java
                    )
                reader.close()
                emit(modelToReturn)

            } catch (e: Exception) {
                emit(e)
            }
        }.onFailure {
            emit(java.lang.Exception(it))

        }
    }.flowOn(Dispatchers.Default)

    fun convertTranslationResponseToEntity(arrayOfTranslationsResponse: List<TranslationData>): ArrayList<TranslationEntity> {
        val iterator = arrayOfTranslationsResponse.iterator()
        val translationEntity: ArrayList<TranslationEntity> = arrayListOf()
        while (iterator.hasNext()) {
            val language: TranslationData = iterator.next()
            val iteratorItems = language.translations.iterator()

            while (iteratorItems.hasNext()) {
                val translations: Translations = iteratorItems.next()

                val entity = TranslationEntity(
                    iso = translations.iso,
                    value = translations.value,
                    key = language.key
                )
                translationEntity.add(entity)
            }
        }
        return translationEntity
    }

    fun convertIsoIntoLanguageEntity(list: ArrayList<String>): ArrayList<LanguageEntity> {
        val languageEntity: ArrayList<LanguageEntity> = arrayListOf()

        for (iso in list) {
            languageEntity.add(LanguageEntity(iso = iso))
        }
        return languageEntity
    }
}