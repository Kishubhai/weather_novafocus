package com.example.weatherapp.forecast

data class weatherCast(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Weather>,
    val message: Int
)