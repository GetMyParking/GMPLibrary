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
        mContext: Context
    ): kotlinx.coroutines.flow.Flow<TranslationResponse?> = flow {
        try {
//            val apiResponse =
//                URL("https://mdn01.lokalise.co/bundles/73982483600e67de5e99c6.59319347/android/472812.json").readText()

            val inputStream = mContext.resources.openRawResource(R.raw.data)

            val inputString = inputStream.bufferedReader().use { it.readText() }
//            println(inputString)
            val gson = Gson()
//            val userArray: ArrayList<TranslationsResponse> =
//                gson.fromJson(inputString, Array<TranslationsResponse>::class.java)
//            translationList.postValue(Result.success(userArray))

            var modelToReturn: TranslationResponse?
            val reader = JsonReader(StringReader(inputString))
            reader.isLenient = true
            modelToReturn =
                gson.fromJson(
                    reader,
                    TranslationResponse::class.java
                )
            reader.close()
            emit(modelToReturn)

        } catch (e: Exception) {
            emit(null)
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
    fun converIsoIntoLanguageEntity(list: ArrayList<String>):ArrayList<LanguageEntity>
    {
        val languageEntity: ArrayList<LanguageEntity> = arrayListOf()

        for (iso in list)
        {
            languageEntity.add(LanguageEntity(iso = iso))
        }
        return languageEntity
    }
}