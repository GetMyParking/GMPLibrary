package com.gmp.gmplocalise.model

data class TranslationResponse
    (
    val data:List<TranslationData>,
    var availableTranslations:ArrayList<String>

)