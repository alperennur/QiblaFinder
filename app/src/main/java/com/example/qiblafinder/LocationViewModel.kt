package com.example.qiblafinder

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.Manifest

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _bearingLiveData = MutableLiveData<BearingData>()
    val bearingLiveData: LiveData<BearingData> get() = _bearingLiveData

    private var currentBearing = 0f
    private var userLocation: Location? = null

    private val targetLocation = Location("target").apply {
        latitude = 21.42280255482353
        longitude = 39.82623768792918
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.takeIf { it.sensor.type == Sensor.TYPE_ROTATION_VECTOR }?.let {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                currentBearing = Math.toDegrees(orientationValues[0].toDouble()).toFloat()

                userLocation?.let { location ->
                    _bearingLiveData.value = BearingData(calculateBearingToTarget(location, targetLocation), currentBearing)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            userLocation = location
            _bearingLiveData.value = BearingData(calculateBearingToTarget(location, targetLocation), currentBearing)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }
    }

    private fun calculateBearingToTarget(userLocation: Location, targetLocation: Location): Float {
        return userLocation.bearingTo(targetLocation)
    }

    fun registerSensorAndLocationUpdates() {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL)

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        }
    }

    fun unregisterSensorAndLocationUpdates() {
        sensorManager.unregisterListener(sensorEventListener)
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener)
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensorAndLocationUpdates()
    }
}
data class BearingData(val bearingToTarget: Float, val currentBearing: Float)
