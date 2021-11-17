package com.gmp.gmplibrary


import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmp.forwardble.BluetoothLeService
import com.gmp.forwardble.ScanLeDevice

import com.gmp.gmplibrary.databinding.ActivityMainBinding
import com.gmp.gmplocalise.GMPLocaliseSdk
import com.gmp.gmplocalise.helper.GmpLocaliseCallBack
import java.util.*

class MainActivity : AppCompatActivity(), ServiceConnection, OnItemClickListener,GmpLocaliseCallBack {
    private var bluetoothDevice: BluetoothDevice? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    lateinit var binder: ActivityMainBinding
    lateinit var adapter: DeviceAdapter
    private var mWriteCharacterstic: BluetoothGattCharacteristic? = null
    val LOCATION_PERMISSION_REQUEST_CODE = 2
    private val REQUEST_ENABLE_BT = 1

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            binder = ActivityMainBinding.inflate(LayoutInflater.from(this))
        GMPLocaliseSdk.initialize(this)
        GMPLocaliseSdk.updateTranslations("",this,Date())
        init()
        setContentView(binder.root)
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    checkBleStatus()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkBleStatus()
                }
                else -> {
                    requestPermission()
                }
            }
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            ScanLeDevice.init(this)
            ScanLeDevice.startScanLeDevice(mLeScanCallbackNew, SCAN_PERIOD)

        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            ScanLeDevice.init(this)
            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            if (ScanLeDevice.getBluetoothAdapter()?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            } else {
                ScanLeDevice.startScanLeDevice(mLeScanCallbackNew, SCAN_PERIOD)

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkBleStatus() {
        ScanLeDevice.init(this)
        if (ScanLeDevice.getBluetoothAdapter()?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableBtIntent,
                REQUEST_ENABLE_BT
            )
        } else {
            ScanLeDevice.startScanLeDevice(mLeScanCallbackNew, SCAN_PERIOD)

        }
    }

    private fun init() {
        binder.rvDeviceList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = DeviceAdapter(this, this)
        binder.rvDeviceList.adapter = adapter
        binder.btnSend.setOnClickListener {
            mWriteCharacterstic?.let {
                mBluetoothLeService?.writeData(binder.etContent.text.toString(), it)
            }
        }
    }

    private val mLeScanCallbackNew: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
//            adapter.addDevice(result.device)
//            adapter.notifyDataSetChanged()
//            binder.progressBar.visibility = View.GONE
//            if (bluetoothDevice == null) {
//                if (result.device.address.equals("wefrgr", true))
//                    bluetoothDevice = result.device
//            }

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            binder.progressBar.visibility = View.GONE
            adapter.addAllDevices(results)
            adapter.notifyDataSetChanged()


        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as BluetoothLeService.LocalBinder
        mBluetoothLeService = binder.getService()
//        connectDevice()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mBluetoothLeService = null
    }

    private fun connectDevice() {
        if (mBluetoothLeService?.initialize() == false) {
            Log.e(
                TAG,
                "Unable to initialize Bluetooth"
            )
            finish()
        }
        ScanLeDevice.stopScanLeDevice(mLeScanCallbackNew)
        mBluetoothLeService?.disconnect()
        // Automatically connects to the device upon successful start-up initialization.
        // Automatically connects to the device upon successful start-up initialization.
        mBluetoothLeService?.connect(bluetoothDevice?.address, mGattCallback)
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                setVisibility(true)
                mBluetoothLeService?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                reScan()

            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: $status")
                mWriteCharacterstic = mBluetoothLeService?.getWriteCharacterStic()

            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

        }
    }

    fun setVisibility(visible: Boolean) {
        runOnUiThread {
            try {
                binder.rvDeviceList.visibility = View.GONE
                binder.llContent.visibility = View.VISIBLE
                var name = "Unknown Device"
                if (bluetoothDevice?.name.equals(""))
                    name = bluetoothDevice?.name.toString()
                binder.tvDeviceName.text = name
                if (visible) {
                    binder.tvConnectionStatus.text = "Status:" + "Connected"
                    binder.btnSend.visibility = View.VISIBLE
                    binder.etContent.visibility = View.VISIBLE
                } else {
                    binder.tvConnectionStatus.text = "Status:" + "Disconnected"
                    binder.btnSend.visibility = View.GONE
                    binder.etContent.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, this, BIND_AUTO_CREATE)
    }

    override fun onItemClick(device: BluetoothDevice) {
        this.bluetoothDevice = device

        if (mBluetoothLeService != null)
            connectDevice()
    }

    override fun onBackPressed() {
        if (binder.rvDeviceList.visibility == View.VISIBLE)
            super.onBackPressed()
        else {
            mBluetoothLeService?.disconnect()

//            reScan()
        }
    }

    fun reScan() {
        runOnUiThread {
            adapter.clearDevices()
            adapter.notifyDataSetChanged()
//        mBluetoothLeService?.disconnect()
            binder.llContent.visibility = View.GONE
            binder.rvDeviceList.visibility = View.VISIBLE
            binder.progressBar.visibility = View.VISIBLE

        }

        ScanLeDevice.startScanLeDevice(mLeScanCallbackNew, SCAN_PERIOD)
    }

    override fun onDBUpdateSuccess() {
        val s= GMPLocaliseSdk.getString("app_name")
        Log.d("Anand", "name=$s")
    }

    override fun onDBUpdateFail() {
    }

    override fun onFileReadSuccess() {
    }

    override fun onFileReadFail(exception: java.lang.Exception) {

    }
}