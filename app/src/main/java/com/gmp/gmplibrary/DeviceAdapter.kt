package com.gmp.gmplibrary

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmp.gmplibrary.databinding.DeviceItemBinding

class DeviceAdapter(private val mContext: Context,private val mOnItemClickListener: OnItemClickListener) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    private var mLeDevices: ArrayList<BluetoothDevice> = ArrayList()

    public class DeviceViewHolder(itemView: View, private val binder: DeviceItemBinding) : RecyclerView.ViewHolder(itemView) {
        var mBinder: DeviceItemBinding = binder
    }
    fun addDevice(device: BluetoothDevice?) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device!!)
        }
    }
    fun clearDevices()
    {
        mLeDevices.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binder=DeviceItemBinding.inflate(LayoutInflater.from(mContext))
        val holder= DeviceViewHolder(binder.root,binder)
        binder.root.setOnClickListener { mOnItemClickListener.onItemClick(mLeDevices[holder.adapterPosition]) }
        return holder
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val bluetoothDevice= mLeDevices[position]
        holder.mBinder.tvDeviceAddress.text=bluetoothDevice.address
        if(bluetoothDevice.name!=null&&!bluetoothDevice.name.equals(""))
        holder.mBinder.tvDeviceName.text=bluetoothDevice.name
        else
            holder.mBinder.tvDeviceName.text="Unknown Device"

    }

    override fun getItemCount(): Int {
        return mLeDevices.size
    }
}