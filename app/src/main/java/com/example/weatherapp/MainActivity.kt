package com.example.weatherapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.forecast.forecastActivity
import com.example.weatherapp.notification.alert
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//3965ac243cac44fb397cb09ef61edf69
class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var city =""

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private val binding:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tv1.setOnClickListener {
            val intent= Intent(this,forecastActivity::class.java)
            startActivity(intent)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permissions
        if (checkLocationPermission()) {
            // Permission already granted, start location updates
            requestLocationUpdates()
        } else {
            // Request location permissions
            requestLocationPermission()
        }

        fetchWeatherData(city)
        SearchCity()


    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Handle the retrieved location
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                // Do something with latitude and longitude
                val locationHelper = LocationHelper(this) // Pass the Activity context
                val cityName = locationHelper.getCityName(latitude, longitude)
                if (cityName != null) {
//                    println("City Name: $cityName")
                    city=cityName.toString().toLowerCase(Locale.ROOT)
                } else {
                    println("Failed to retrieve city name.")
                }
            } else {
                // Location not available, handle the case
            }
        }
    }

//    fun getCityName(latitude: Double, longitude: Double): String? {
//        val geocoder = Geocoder(context, Locale.getDefault())
//        try {
//            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
//            if (addresses.isNotEmpty()) {
//                val address: Address = addresses[0]
//                // The "locality" field typically contains the city name
//                val cityName: String? = address.locality
//                return cityName
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return null
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                requestLocationUpdates()
            } else {
                // Permission denied, handle the case
            }
        }
    }


    private fun SearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit =Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName,"3965ac243cac44fb397cb09ef61edf69","metric")
        response.enqueue(object :Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody?.main?.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

//                    Log.d("TAG", "onResponse: $temperature")
                    binding.temperature.text ="$temperature °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max Temp: $maxTemp°C"
                    binding.minTemp.text = "Min Temp: $minTemp°C"
                    binding.humidity.text = "$humidity"
                    binding.windSpeed.text = "$windSpeed"
                    binding.sunrise.text = "${time(sunRise)}"
                    binding.sunset.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hpa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text =date()
                        binding.cityName.text = "$cityName"

                    changeImageAccordingToWeatherCondition(condition)

                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {

            }

        })



    }

    private fun changeImageAccordingToWeatherCondition(condition: String) {
        when(condition){
            "Clear Sky","Sunny","Clear"->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)

                // Create an intent for the full-screen activity
                // Create an intent for the full-screen activity
                val fullScreenIntent = Intent(this, alert::class.java)
                val fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                    fullScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

// Create the notification
                // Access channel ID from resources
                val CHANNEL_ID = getString(R.string.channel_id)

                // Define the channel ID (same as you've used before)
                val channelId = getString(R.string.channel_id)
                createNotificationChannel()
                val channelname=R.string.channel_name
                val channeldescription=R.string.channel_description
// Create the notification channel (if not already created)



                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.cloud_black)
                    .setContentTitle("Weather Alert")
                    .setContentText("Heavy Rainfall")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(fullScreenPendingIntent, true)

// Show the notification
                val notificationManager = NotificationManagerCompat.from(this)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                val notificationId =1
                notificationManager.notify(notificationId, builder.build())


            }
            "Partly Cloud","Clouds","Overcast","Mist","Foggy"->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain"->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard"->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.channel_id)
            val channelName = getString(R.string.channel_name)

            val channelDescription = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timeStamp:Long):String{
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timeStamp*1000)))
    }
    fun dayName(timeStamp:Long):String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}