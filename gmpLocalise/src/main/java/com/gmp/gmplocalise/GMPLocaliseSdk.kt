package com.gmp.gmplocalise

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import com.gmp.gmplocalise.database.GmpLocaliseDatabase
import com.gmp.gmplocalise.helper.GmpLocaliseCallBack
import com.gmp.gmplocalise.model.TranslationResponse
import com.gmp.gmplocalise.utils.ParserUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
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

        return if (lastCacheDate <= 0) {
            true
        } else {
            return if (lastUpdateDate.after(Date(lastCacheDate))) {
                true
            } else {
                setLocality()
                false
            }
        }
    }

    @JvmStatic
    fun updateTranslations(
        s3Url: String,
        mCallBack: GmpLocaliseCallBack,
        lastUpdateDate: Date
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
                        setLocality()
                        with(sharedPreferences?.edit()) {
                            this?.putLong("updated_date", lastUpdateDate.time)
                            this?.apply()
                        }
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


    private fun getAvailableLanguages(): List<String>? {
        var languages: List<String>?
        runBlocking {
            languages = dbInstance?.translationDao()
                ?.getAllLanguages()
        }
        return languages
    }

    private fun setLocality() {
        val availableTranslations = getAvailableLanguages()
        if (!availableTranslations.isNullOrEmpty()) {
            val locale = getCurrentLocales()
            var locality =
                availableTranslations.filter {
                    it.contains(
                        locale.toString().replace("_", "-"),
                        true
                    )
                }
            if (!locality.isNullOrEmpty())
                setLocality(locality[0])
            else {
                locality =
                    availableTranslations.filter {
                        it.contains(
                            locale.language,
                            true
                        )
                    }
                if (!locality.isNullOrEmpty())
                    setLocality(locality[0])
                else
                    setLocality(defaultIso)
            }
        }
    }

    private fun setLocality(iso: String) {
        with(sharedPreferences?.edit()) {
            this?.putString("iso", iso)
            this?.apply()
        }
    }

    private fun getCurrentLocales(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            Resources.getSystem().configuration.locale
        }
    }


}