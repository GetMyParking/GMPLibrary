package com.gmp.forwardble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Handler
import android.os.Looper

object ScanLeDevice {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    var bluetoothLeScanner: BluetoothLeScanner? = null
    private const val REQUEST_ENABLE_BT = 1
    // Stops scanning after 10 seconds.
    private const val SCAN_PERIOD: Long = 10000
    private var mScanning = false
    private var mHandler: Handler? = null
    fun init(mContext: Context) {
        mHandler = Handler(Looper.myLooper()!!)

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.

        val bluetoothManager =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = mBluetoothAdapter?.bluetoothLeScanner
    }
    fun getBluetoothAdapter():BluetoothAdapter?
    {
        return mBluetoothAdapter
    }
     fun startScanLeDevice(scanCallback: ScanCallback) {

            // Stops scanning after a pre-defined scan period.
            mHandler?.postDelayed(Runnable {
                mScanning = false
                bluetoothLeScanner?.stopScan(scanCallback)
            }, SCAN_PERIOD)
            mScanning = true
            bluetoothLeScanner?.startScan(scanCallback)

    }
    fun stopScanLeDeviceg(scanCallback: ScanCallback)
    {
        bluetoothLeScanner?.stopScan(scanCallback)

    }

}