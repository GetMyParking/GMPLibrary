package com.gmp.gmplokalise.model

data class TranslationResponse
    (
    val data:List<TranslationData>,
    var availableTranslations:ArrayList<String>

)