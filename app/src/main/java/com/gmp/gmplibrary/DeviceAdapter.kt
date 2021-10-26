package com.gmp.gmplibrary

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmp.gmplibrary.databinding.DeviceItemBinding

class DeviceAdapter(
    private val mContext: Context,
    private val mOnItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    private var mLeDevices: MutableList<ScanResult> = mutableListOf()

    class DeviceViewHolder(itemView: View, private val binder: DeviceItemBinding) :
        RecyclerView.ViewHolder(itemView) {
        var mBinder: DeviceItemBinding = binder
    }

    fun addDevice(device: BluetoothDevice?) {
//        if (!mLeDevices.contains(device)) {
//            mLeDevices.add(device!!)
//        }
    }

    fun addAllDevices(results: List<ScanResult>) {
        if (mLeDevices.size == 0)
            mLeDevices.addAll(results)
        else{
            mLeDevices.clear()
            mLeDevices.addAll(results)
        }

    }

    fun clearDevices() {
        mLeDevices.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binder = DeviceItemBinding.inflate(LayoutInflater.from(mContext))
        val holder = DeviceViewHolder(binder.root, binder)
        binder.root.setOnClickListener { mOnItemClickListener.onItemClick(mLeDevices[holder.adapterPosition].device) }
        return holder
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val bluetoothDevice = mLeDevices[position]
        holder.mBinder.tvDeviceAddress.text = bluetoothDevice.device.address
        if (bluetoothDevice.device.name != null && !bluetoothDevice.device.name.equals(""))
            holder.mBinder.tvDeviceName.text = bluetoothDevice.device.name
        else
            holder.mBinder.tvDeviceName.text = "Unknown Device"

    }

    override fun getItemCount(): Int {
        return mLeDevices.size
    }
}