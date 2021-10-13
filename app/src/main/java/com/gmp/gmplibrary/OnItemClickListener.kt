package com.gmp.gmplibrary

import android.bluetooth.BluetoothDevice
import java.text.FieldPosition

interface OnItemClickListener {
    fun onItemClick(device: BluetoothDevice)
}