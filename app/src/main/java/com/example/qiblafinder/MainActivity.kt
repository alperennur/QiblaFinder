package com.example.qiblafinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.qiblafinder.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationViewModel: LocationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val compassDirection = 0
        binding.arrowImage.rotation = compassDirection.toFloat()
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationViewModel.bearingLiveData.observe(this, Observer { bearingData ->
            updateUI(bearingData.bearingToTarget, bearingData.currentBearing)
        })

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.registerSensorAndLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationViewModel.unregisterSensorAndLocationUpdates()
    }

    private fun updateUI(bearingToTarget: Float, currentBearing: Float) {
        val rotationDegree = bearingToTarget - currentBearing
        if (rotationDegree>-1f && rotationDegree<1f){
            binding.arrowImage.setImageResource(R.drawable.arrow_true)
            binding.kaabaImage.visibility = View.VISIBLE
        }
        else{
            binding.arrowImage.setImageResource(R.drawable.arrow_false)
            binding.kaabaImage.visibility = View.INVISIBLE
        }
        binding.arrowImage.rotation = rotationDegree
        println(rotationDegree)

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}