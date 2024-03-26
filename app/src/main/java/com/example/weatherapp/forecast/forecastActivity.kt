package com.example.weatherapp.forecast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.weatherapp.ApiInterface
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApp
import com.example.weatherapp.databinding.ActivityForecastBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class forecastActivity : AppCompatActivity() {
    private val binding:ActivityForecastBinding by lazy {
        ActivityForecastBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData(44.34,10.99)
    }

    private fun fetchWeatherData(d: Double, d1: Double) {

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(apiforecast::class.java)

        val response = retrofit.getWeatherData(d,d1,"3965ac243cac44fb397cb09ef61edf69","metric")
        response.enqueue(object : Callback<fore> {
            override fun onResponse(call: Call<fore>, response: Response<fore>) {
                val responseBody = response.body()
                val temperature = responseBody?.main?.temp_max.toString()

//                Log.d("TAG", "onResponse: $temperature")
            }

            override fun onFailure(call: Call<fore>, t: Throwable) {
                TODO("Not yet implemented")
            }


        })

    }
}