package com.gmp.gmplokalise.helper

import java.lang.Exception

interface GmpLokaliseCallBack {
    fun onDBUpdateSuccess()
    fun onDBUpdateFail(exception: Exception)
    fun onFileReadSuccess()
    fun onFileReadFail(exception: Exception)
}