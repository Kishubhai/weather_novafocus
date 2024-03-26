package com.example.weatherapp.forecast

import com.example.weatherapp.WeatherApp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface apiforecast {

    @GET("forecast")
    fun getWeatherData(
        @Query("q") lat:Double,
        @Query("q") lon:Double,
        @Query("appid") appid:String,
        @Query("units") units:String
    ): Call<fore>
}