package com.gmp.gmplocalise

import android.content.Context
import android.content.SharedPreferences
import com.gmp.gmplocalise.database.GmpLocaliseDatabase
import com.gmp.gmplocalise.helper.GmpLocaliseCallBack
import com.gmp.gmplocalise.model.TranslationResponse
import com.gmp.gmplocalise.utils.ParserUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.lang.Exception
import java.util.*


object GMPLocaliseSdk {
    var scope = CoroutineScope(Job() + Dispatchers.IO)
    var parserUtils: ParserUtils? = null
    var sharedPreferences: SharedPreferences? = null
    var lastCacheDate: Long = 0
    var defaultIso = "en"
    var dbInstance: GmpLocaliseDatabase? = null
    var values: List<String>? = null
    var mCallBack: GmpLocaliseCallBack? = null

    init {
        parserUtils = ParserUtils()
    }

    @JvmStatic
    fun initialize(mContext: Context) {
        sharedPreferences = mContext.getSharedPreferences(
            "Gmp_Localise_Pref", Context.MODE_PRIVATE
        )
        dbInstance = GmpLocaliseDatabase.getInstance(mContext)

    }

    @JvmStatic
    fun isTranslationUpdateAvailable(lastUpdateDate: Date): Boolean {

        sharedPreferences?.getLong("updated_date", 0L)?.let {
            lastCacheDate = it
        }
        with(sharedPreferences?.edit()) {
            this?.putLong("updated_date", lastUpdateDate.time)
            this?.apply()
        }
        return if (lastCacheDate <= 0) {
            true
        } else {
            lastUpdateDate.before(Date(lastCacheDate))
        }
    }

    @JvmStatic
    fun updateTranslatuions(
        s3Url: String,
        mCallBack: GmpLocaliseCallBack
    ) {
        this.mCallBack = mCallBack


        scope.launch {

            parserUtils?.parseData(s3Url)?.collect {
                if (it != null && it is TranslationResponse) {
                    mCallBack.onFileReadSuccess()
                    val translationEntityList =
                        parserUtils?.convertTranslationResponseToEntity(it.data)
                    if (!translationEntityList.isNullOrEmpty()) {
                        dbInstance?.clearAllTables()
                        parserUtils?.convertIsoIntoLanguageEntity(it.availableTranslations)
                            ?.let { it1 ->
                                dbInstance?.translationDao()
                                    ?.insertIso(it1)
                            }
//                            GmpLocaliseDatabase.getInstance(mContext)?.translationDao()?.removeAll()
                        dbInstance?.translationDao()
                            ?.updateSequence()
                        dbInstance?.translationDao()
                            ?.insertTranslations(translationEntityList)

                        mCallBack.onDBUpdateSuccess()

                    } else {
                        mCallBack.onDBUpdateFail()
                    }
                } else if (it is Exception) {
                    mCallBack.onFileReadFail(it)
                }
            }

        }
    }

    @JvmStatic
    fun setLocality(iso: String) {
        with(sharedPreferences?.edit()) {
            this?.putString("iso", iso)
            this?.apply()
        }
    }

    @JvmStatic
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

    @JvmStatic
    fun getLocality(): String {
        var iso = defaultIso
        sharedPreferences?.getString("iso", defaultIso)?.let {
            iso = it
        }
        return iso
    }

    @JvmStatic
    fun setDefaultLocality(defaultIso: String) {
        this.defaultIso = defaultIso
    }

    @JvmStatic
    fun getAvailableLanguages(): List<String>? {
        var languages: List<String>?
        runBlocking {
            languages = dbInstance?.translationDao()
                ?.getAllLanguages()
        }
        return languages
    }

}