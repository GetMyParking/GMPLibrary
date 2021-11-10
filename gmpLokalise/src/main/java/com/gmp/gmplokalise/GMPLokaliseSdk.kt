package com.gmp.gmplokalise

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gmp.gmplokalise.database.GmpLokaliseDatabase
import com.gmp.gmplokalise.helper.GmpLokaliseCallBack
import com.gmp.gmplokalise.model.LanguageEntity
import com.gmp.gmplokalise.utils.ParserUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


object GMPLokaliseSdk {
    var scope = CoroutineScope(Job() + Dispatchers.IO)
    var parserUtils: ParserUtils? = null
    var sharedPreferences: SharedPreferences? = null
    var oldVersion = 0
    var defaultIso = "en"
    var dbInstance: GmpLokaliseDatabase? = null
    var values: List<String>? = null
    var mCallBack: GmpLokaliseCallBack? = null

    init {
        parserUtils = ParserUtils()
    }

    @JvmStatic
    fun initialize(
        s3Url: String,
        newVersion: Int,
        mContext: Context,
        mCallBack: GmpLokaliseCallBack
    ) {
        this.mCallBack = mCallBack
        sharedPreferences = mContext.getSharedPreferences(
            "Gmp_Lokalise_Pref", Context.MODE_PRIVATE
        )
        sharedPreferences?.getInt("version", 0)?.let {
            oldVersion = it
        }
        if (newVersion >= oldVersion) {
            with(sharedPreferences?.edit()) {
                this?.putInt("version", newVersion)
                this?.apply()
            }
            scope.launch {

                parserUtils?.parseData(mContext)?.collect {
                    if (it != null) {
                        val translationEntityList =
                            parserUtils?.convertTranslationResponseToEntity(it.data)
                        if (!translationEntityList.isNullOrEmpty()) {
                            dbInstance = GmpLokaliseDatabase.getInstance(mContext)
                            dbInstance?.clearAllTables()

//                            GmpLokaliseDatabase.getInstance(mContext)?.translationDao()?.removeAll()
                            dbInstance?.translationDao()
                                ?.updateSequence()
                            dbInstance?.translationDao()
                                ?.insertTranslations(translationEntityList)
                            parserUtils?.converIsoIntoLanguageEntity(it.availableTranslations)
                                ?.let { it1 ->
                                    dbInstance?.translationDao()
                                        ?.insertIso(it1)
                                }

                        }
                    }
                }
            }
        }
    }

    fun setLocality(iso: String) {
        with(sharedPreferences?.edit()) {
            this?.putString("iso", iso)
            this?.apply()
        }
    }

//    fun getString(key: String) = CoroutineScope(Dispatchers.IO).async {
//        var iso = defaultIso
//        sharedPreferences?.getString("iso", defaultIso)?.let {
//            iso = it
//        }
//        val values = dbInstance?.translationDao()
//            ?.getString(key, iso)
//        if (!values.isNullOrEmpty())
//            return@async values[0]
//        return@async ""
//    }

    fun getString(key: String): String {
        var iso = defaultIso
        sharedPreferences?.getString("iso", defaultIso)?.let {
            iso = it
        }
        runBlocking {

            values = dbInstance?.translationDao()
                ?.getString(key, iso)
        }
        if (!values.isNullOrEmpty())
            return values!![0]

        return ""

    }

    fun getLocality(): String {
        var iso = defaultIso
        sharedPreferences?.getString("iso", defaultIso)?.let {
            iso = it
        }
        return iso
    }

    fun setDefaultLocality(defaultIso: String) {
        this.defaultIso = defaultIso
    }

    fun getAvailableLanguages(): List<String>? {
        var languages: List<String>?
        runBlocking {
            languages = dbInstance?.translationDao()
                ?.getAllLanguages()
        }
        return languages
    }

}