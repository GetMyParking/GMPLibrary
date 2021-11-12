package com.gmp.gmplocalise.helper

import java.lang.Exception

interface GmpLocaliseCallBack {
    fun onDBUpdateSuccess()
    fun onDBUpdateFail()
    fun onFileReadSuccess()
    fun onFileReadFail(exception: Exception)
}