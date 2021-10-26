package com.gmp.forwardble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper


object ScanLeDevice {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanning = false
    private var mHandler: Handler? = null

    @JvmStatic
    fun init(mContext: Context) {
        mHandler = Handler(Looper.myLooper()!!)

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.

        val bluetoothManager =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = mBluetoothAdapter?.bluetoothLeScanner
    }

    @JvmStatic
    fun getBluetoothAdapter(): BluetoothAdapter? {
        return mBluetoothAdapter
    }

    //Start Scanning Le Devices
    @JvmStatic
    fun startScanLeDevice(scanCallback: ScanCallback, scanPeriod: Long) {

        // Stop scanning after a pre-defined scan period.
        mHandler?.postDelayed(Runnable {
            mScanning = false
            bluetoothLeScanner?.stopScan(scanCallback)
        }, scanPeriod)
        mScanning = true
//scan with filter (scan result will be in batch)
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(1000)
            .build()

        val filters = mutableListOf<ScanFilter>()
        bluetoothLeScanner?.startScan(filters, settings, scanCallback)

        //scan without filter
//        bluetoothLeScanner?.startScan(scanCallback)

    }

    // Stop Scanning Le Devices
    @JvmStatic
    fun stopScanLeDevice(scanCallback: ScanCallback) {
        bluetoothLeScanner?.stopScan(scanCallback)
    }

}