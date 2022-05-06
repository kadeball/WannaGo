package com.bignerdranch.android.wannago.ui.Location2

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.wannago.Location
import com.bignerdranch.android.wannago.PermissionUtils
import com.bignerdranch.android.wannago.R
import com.bignerdranch.android.wannago.SwipeToDeleteCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationFragment2 : Fragment(), GoogleMap.OnMapClickListener, OnMapReadyCallback {

    companion object {
        fun newInstance() = LocationFragment2()
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
    }

    private lateinit var viewModel: LocationFragment2ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                for (location in locationResult!!.locations) {

                }
                if (ActivityCompat.checkSelfPermission(
                        context!!, android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context!!,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
        }
    }

    private lateinit var locrecyclerview: RecyclerView
    private val locationViewModel: LocationFragment2ViewModel by viewModels()
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.location_fragment2, container, false)
        locrecyclerview = view.findViewById(R.id.fragment_location2) as RecyclerView
        locrecyclerview.layoutManager = LinearLayoutManager(context)
        locrecyclerview.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        locationViewModel.listenToCollection("locations2")
        mapView = view.findViewById(R.id.locMapView2)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        val swipeHandler = object: SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = locrecyclerview.adapter as LocationAdapter
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(locrecyclerview)

        return view
    }

    private fun setUpLocationListener() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

        // CREATE A HIGH-ACCURACY LOCATIONREQUEST THAT CHECKS LOCATION EVERY 2 SECONDS AT HIGH ACCURACY

        val locationRequest = LocationRequest()
            .setInterval(2000)
            .setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)


        // PASS THAT LOCATIONREQUEST OBJECT TO THE FUSEDLOCATIONPROVIDER
        if (ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
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
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )

        // WHEN WE GET A LOCATION BACK FROM THE PROVIDER...

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // CHECK TO SEE IF THE RESPONSE IS FOR THE PERMISSION CHECK WE MADE (999)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            // CHECK TO SEE IF THE USER HAS GIVEN US PERMISSION TO USE THEIR LOCATION
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                // CHECK TO SEE IF THE GPS ON THE DEVICE IS ENABLED
                if (PermissionUtils.isLocationEnabled((context!!))) {
                    setUpLocationListener()
                }
                // IF NO GPS, SHOW A DIALOG
                else {
                    PermissionUtils.showGPSNotEnabledDialog(context!!)
                }

            }

            // IF NO PERMISSIONS, ASK FOR PERMISSION
            else{
                Toast.makeText(context!!,
                    getString(R.string.location_permission_not_granted),
                    Toast.LENGTH_LONG).show()
            }

        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocationFragment2ViewModel::class.java)
        locationViewModel.listOfLocations.observe(viewLifecycleOwner, {
            updateUI()
        })
        // TODO: Use the ViewModel
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()

        // CHECK TO SEE IF THE USER HAS GIVEN US PERMISSION TO USE THEIR LOCATION
        if (PermissionUtils.isAccessFineLocationGranted(context!!)) {
            // CHECK TO SEE IF THE GPS ON THE DEVICE IS ENABLED
            if (PermissionUtils.isLocationEnabled(context!!)) {
                setUpLocationListener()
            }
            // IF NO GPS, SHOW A DIALOG
            else {
                PermissionUtils.showGPSNotEnabledDialog(context!!)
            }
        }
        // IF NO PERMISSIONS, ASK FOR PERMISSION
        else {
            PermissionUtils.requestAccessFineLocationPermission(
                this,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
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
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            val getLocation = LatLng(it.latitude, it.longitude)
            googleMap?.addMarker(MarkerOptions().position(getLocation))

            val cameraPosition = CameraPosition.Builder().target(getLocation).zoom(14f).tilt(45f).build()
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        }
    }


    private fun updateUI() {
        locrecyclerview.adapter = LocationAdapter(locationViewModel.listOfLocations.value!!)
    }

    private inner class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var locationItemTextView: TextView = view.findViewById(R.id.location2)
        fun bind(location: Location) {
            locationItemTextView.text =
                location.lat.toString() + ", " + location.long.toString()
            googleMap?.addMarker(
                MarkerOptions().position(LatLng(location.lat, location.long))
            )
        }
    }

    private inner class LocationAdapter(var datatoAdapt: ArrayList<Location>) :
        RecyclerView.Adapter<LocationViewHolder>
            () {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
            val recyclerViewItem = LayoutInflater.from(parent.context).inflate(R.layout.list_item_location2, parent, false)
            return LocationViewHolder(recyclerViewItem)
        }

        override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
            holder.bind(datatoAdapt[position])
        }

        override fun getItemCount(): Int {
            return datatoAdapt.size
        }

        fun removeAt(position: Int) {
            locationViewModel.deletefromFirestore(datatoAdapt[position].documentId, "locations2")
            datatoAdapt.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    override fun onMapClick(point: LatLng) {
        locationViewModel.writeToFirestore(point.latitude, point.longitude, "locations2")
        googleMap?.addMarker(MarkerOptions().position(point))

        val cameraPosition = CameraPosition.Builder().target(point).zoom(14f).tilt(45f).build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        googleMap?.uiSettings?.isZoomControlsEnabled = true
    }


    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        googleMap?.setOnMapClickListener(this)
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        mapView.onResume()

    }

}