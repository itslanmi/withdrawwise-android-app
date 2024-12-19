package com.example.withdrawwise

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.data.ATM
import com.example.data.Bank

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.withdrawwise.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory

private lateinit var fusedLocationClient: FusedLocationProviderClient
private val LOCATION_PERMISSION_REQUEST_CODE = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var selectedATM: ATM
    private var isATMSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mapFragment.getMapAsync(this)

        binding.btnProfile.setOnClickListener() {
            val intent = Intent(this, ChartActivity::class.java)
            startActivity(intent)
        }

        binding.btnInfo.setOnClickListener() {
            if(isATMSelected) {
                showNotification()
            }else{
                Log.d("MapsActivity", "No ATM selected")
                Toast.makeText(this, "Please select an ATM first", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnAdd.setOnClickListener(){
            if(isATMSelected){
                val intent = Intent(this, AddWithdrawalActivity::class.java)
                intent.putExtra("selectedATM", selectedATM.name)
                startActivity(intent)
            }else{
                Log.d("MapsActivity", "No ATM selected")
                Toast.makeText(this, "Please select an ATM first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "profile_channel",
                "Profile Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open ProfileActivity
        val mapsIntent = Intent(this, MapsActivity::class.java)
        val profileIntent = Intent(this, ChartActivity::class.java)
        val pendingIntent = PendingIntent.getActivities(
            this, 0, arrayOf(mapsIntent, profileIntent), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, "profile_channel")
            .setSmallIcon(R.drawable.dummy_profile_pic) // Replace with your notification icon
            .setContentTitle("Profile Notification")
            .setContentText("Time to check your Withdrawal statistics!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }

        // Add markers for all ATMs from the list in MyApplication
        val app = application as MyApplication
        for (atm in app.ATMlist) {
            val atmLocation = LatLng(atm.latitude, atm.longitude)
            mMap.addMarker(MarkerOptions().position(atmLocation).title(atm.name))?.tag = atm
        }


        mMap.setOnMarkerClickListener { marker ->
            val atm = marker.tag as? ATM
            if (atm != null) {
                selectedATM = atm
                isATMSelected = true

                Log.d("MapsActivity", "Selected ATM: ${selectedATM.name}, Location: ${selectedATM.latitude}, ${selectedATM.longitude}")
                //marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.location))
                marker.showInfoWindow()

            }
            true
        }

        mMap.setOnInfoWindowCloseListener {
            isATMSelected = false
        }

    }
}
