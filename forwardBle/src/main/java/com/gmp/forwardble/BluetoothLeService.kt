package com.gmp.forwardble

import android.annotation.TargetApi
import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.util.*

public class BluetoothLeService : Service() {
    private val TAG: String =
        BluetoothLeService::class.java.getSimpleName()

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null

    private val mBinder: IBinder =
        LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {

        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BluetoothLeService = this@BluetoothLeService
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }
        mBluetoothAdapter = mBluetoothManager?.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun connect(address: String?, mGattCallback: BluetoothGattCallback): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(
                TAG,
                "BluetoothAdapter not initialized or unspecified address."
            )
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            Log.d(
                TAG,
                "Trying to use an existing mBluetoothGatt for connection."
            )
            return mBluetoothGatt?.connect() == true
        }
        val device = mBluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt =
            device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        return true
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    private fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return if (mBluetoothGatt == null) null else mBluetoothGatt?.services
    }

    fun writeData(msg: String, characteristic: BluetoothGattCharacteristic) {

        try {
            characteristic.value = msg.toByteArray()
            val status = mBluetoothGatt?.writeCharacteristic(characteristic)
            Log.d(TAG, "status=$status")
        } catch (e: Exception) {
            Log.e(
                "LOG_TAG",
                "BluetoothLowEnergy.send: Failed to convert message string to byte array"
            )
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothDeviceAddress = null
        mBluetoothGatt?.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt?.close()
        mBluetoothGatt = null
        mBluetoothDeviceAddress = null

    }

     fun getWriteCharacterStic(): BluetoothGattCharacteristic? {
        var mCharacterstic: BluetoothGattCharacteristic? = null
        val gattServices = getSupportedGattServices() ?: return mCharacterstic
        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val gattCharacteristics = gattService?.characteristics
            // Loops through available Characteristics.
            if (gattCharacteristics != null) {
                for (gattCharacteristic in gattCharacteristics) {
                    if (gattCharacteristic.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                        mCharacterstic = gattCharacteristic
                        break
                    }
                }
            }
            if (mCharacterstic != null) {
                break;
            }
        }
        return mCharacterstic

    }
    fun discoverServices()
    {
        mBluetoothGatt?.discoverServices()

    }
}
