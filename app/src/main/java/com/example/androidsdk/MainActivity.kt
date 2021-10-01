package com.example.androidsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.androidsdk.databinding.ActivityMainBinding
import com.example.customdialog.CustomDialogObj

class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }
    fun onClick(view:View) {
        when (view) {
            binding.btnDialog->CustomDialogObj.showDialog(this,
                "Get My Parking",
                "sdk creation test message"
            ) {
                Toast.makeText(this, "dismiss", Toast.LENGTH_SHORT).show()
            }
        }
    }
}