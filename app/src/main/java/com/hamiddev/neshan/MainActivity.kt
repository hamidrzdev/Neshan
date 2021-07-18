package com.hamiddev.neshan

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carto.styles.AnimationStyle
import com.carto.styles.AnimationStyleBuilder
import com.carto.styles.AnimationType
import com.carto.styles.MarkerStyleBuilder
import com.google.android.gms.location.*
import com.hamiddev.neshan.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.internal.utils.BitmapUtils
import org.neshan.mapsdk.model.Marker


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var animSt: AnimationStyle
    private var marker: Marker? = null
    private var isGranted = false
    private lateinit var location: Location
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    location = it.lastLocation
                    onLocationChanged(LatLng(it.lastLocation.latitude, it.lastLocation.longitude))
                    Toast.makeText(
                        this@MainActivity,
                        "last location is : " + it.lastLocation.latitude + "," + it.lastLocation.longitude,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
        binding.locateBtn.setOnClickListener {
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    onLocationChanged(LatLng(it.latitude, it.longitude))
                }
            }
        }


        binding.map.setOnMapClickListener {
            onLocationChanged(it)
        }

        initMap()

    }

    override fun onStart() {
        super.onStart()
        getPermission()
        if (!isGranted) {
            getPermission()
            return
        }
    }

    fun initMap() {
        binding.map.isPoiEnabled = true
        showTrafficLayer()
    }

    override fun onResume() {
        super.onResume()
        startUpdatingLocation()
    }

    override fun onPause() {
        super.onPause()
        stopUpdatingLocation()
    }

    fun onLocationChanged(latLng: LatLng) {
        binding.map.addMarker(createMarker(latLng))
        moveCamera(latLng)
    }

    fun createMarker(location: LatLng): Marker {

        if (marker != null)
            binding.map.removeMarker(marker)

        val animStBl = AnimationStyleBuilder()
        animStBl.fadeAnimationType = AnimationType.ANIMATION_TYPE_SMOOTHSTEP
        animStBl.sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
        animStBl.phaseInDuration = 0.5f
        animStBl.phaseOutDuration = 0.5f
        animSt = animStBl.buildStyle()

        val markStCr = MarkerStyleBuilder()
        markStCr.size = 30f
        markStCr.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(
            BitmapFactory.decodeResource(
                resources, R.drawable.marker
            )
        )
        markStCr.animationStyle = animSt
        val markSt = markStCr.buildStyle()

        val myMarker = Marker(location, markSt)
        marker = myMarker

        // Creating marker
        return myMarker
    }

    fun getPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        isGranted = it.areAllPermissionsGranted()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    fun moveCamera(latLng: LatLng) {
        binding.map.moveCamera(latLng, 1.5F)
        binding.map.setZoom(13f, 0.5f)
    }

    @SuppressLint("MissingPermission")
    fun startUpdatingLocation() {
        locationClient.requestLocationUpdates(LocationRequest(), locationCallback, null)
    }

    fun stopUpdatingLocation() {
        locationClient.removeLocationUpdates(locationCallback)
    }

    fun showTrafficLayer() {
        binding.trafficSwitch.setOnClickListener {
            binding.map.isTrafficEnabled = binding.trafficSwitch.isChecked
        }
    }

}