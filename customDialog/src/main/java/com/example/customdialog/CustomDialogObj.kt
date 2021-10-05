package com.example.customdialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object CustomDialogObj {
    fun showDialog(
        mContext: Context,
        title: String,
        message: String,
        onPositiveButtonCallBack: () -> Unit
    ) {
        AlertDialog.Builder(mContext)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("dismiss", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    onPositiveButtonCallBack.invoke()
                }
            }).show()
    }
    fun dismiss()
    {

    }
}