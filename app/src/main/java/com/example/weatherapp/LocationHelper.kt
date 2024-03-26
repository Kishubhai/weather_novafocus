package com.example.weatherapp

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*

class LocationHelper(private val context: Context) {

    fun getCityName(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val cityName: String? = address.locality
                return cityName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

