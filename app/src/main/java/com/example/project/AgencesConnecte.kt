package com.example.project

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.project.models.Agence
import com.example.project.models.GAB
import com.example.project.viewmodels.AgencesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgencesConnecte : Fragment(), OnMapReadyCallback {

    private val agencesViewModel: AgencesViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var mView: View
    private lateinit var tabLayout: TabLayout
    private var currentAction = "getAgencesNear"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = inflater.inflate(R.layout.agences, container, false)
        tabLayout = mView.findViewById(R.id.tabLayout)
        setupTabTitles()
        setupMapFragment()
        setupTabListener()


        return mView
    }

    private fun setupTabTitles() {
        val tabTitles = arrayOf("Agences", "Guichets Automatiques", "Wafacash")
        tabTitles.forEachIndexed { index, title ->
            tabLayout.addTab(tabLayout.newTab().apply {
                customView = LayoutInflater.from(context).inflate(R.layout.custom_tab_view, null).apply {
                    findViewById<TextView>(R.id.custom_text).apply {
                        text = title
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_indicator)
                    }
                }
            })
        }
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupObservers() {
        agencesViewModel.agences.observe(viewLifecycleOwner, Observer { agences ->
            if (agences.isNotEmpty()) {
                updateMapMarkers(agences)
            } else {
                Log.d("LiveData", "No agences available to display.")
            }
        })

        agencesViewModel.gabs.observe(viewLifecycleOwner, Observer { gabs ->
            if (gabs.isNotEmpty()) {
                updateGABMarkers(gabs)
            } else {
                Log.d("LiveData", "No GABs available to display.")
            }
        })
    }


    private fun setupTabListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateForTabSelection(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                updateForTabSelection(tab.position)
            }
        })
    }

    private fun updateForTabSelection(position: Int) {
        currentAction = when (position) {
            0 -> "getAgencesNear"
            1 -> "getGABNear"
            2 -> "getAgencesWafacashNear"
            else -> "getAgencesNear"
        }
        Log.d("TabSelection", "Current Action: $currentAction")

        loadAgencesForCurrentPosition()
    }

    private fun loadAgencesForCurrentPosition() {
        val center = mMap.cameraPosition.target
        val radius = 10000.0 // Define your radius as required
        lifecycleScope.launch {
            when (currentAction) {
                "getAgencesNear", "getAgencesWafacashNear" -> {
                    agencesViewModel.loadAgencesWithinRadius(
                        center.latitude,
                        center.longitude,
                        radius,
                        currentAction
                    )
                }

                "getGABNear" -> {
                    agencesViewModel.loadGABWithinRadius(
                        center.latitude,
                        center.longitude,
                        radius,
                        currentAction
                    )
                }
            }
        }
    }

    private fun updateMapMarkers(agences: List<Agence>) {
        mMap.clear()
        agences.forEach { agence ->
            try {
                val position = LatLng(agence.latitude.toDouble(), agence.longitude.toDouble())
                val markerOptions = MarkerOptions().position(position).title(agence.nom)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(getIconForResource(currentAction)))
                mMap.addMarker(markerOptions)
                mMap.addMarker(markerOptions)?.tag = agence
            } catch (e: Exception) {
                Log.e("UpdateMapMarkers", "Error creating marker for agence: ${agence.nom}", e)
            }
        }
    }


    private fun updateGABMarkers(gabs: List<GAB>) {
        mMap.clear()
        gabs.forEach { gab ->
            try {
                val position = LatLng(gab.latitude.toDouble(), gab.longitude.toDouble())
                val markerOptions = MarkerOptions().position(position).title(gab.nom)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(getIconForResource(currentAction)))
                mMap.addMarker(markerOptions)?.tag = gab
            } catch (e: Exception) {
                Log.e("UpdateGABMarkers", "Error creating marker for GAB: ${gab.nom}", e)
            }
        }
    }


    private fun getIconForResource(action: String): Int {
        return when (action) {
            "getGABNear" -> R.drawable.atm
            "getAgencesWafacashNear" -> R.drawable.cash
            else -> R.drawable.agence
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap()
        setupObservers()
    }

    private fun setupMap() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentUserLocation = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f))
                } ?: run {
                    setDefaultLocation()
                }
            }.addOnFailureListener {
                Log.e("Location Error", "Failed to get location, defaulting to default location.")
                setDefaultLocation()
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        loadAgencesForCurrentPosition()

        mMap.setOnMarkerClickListener { marker ->
            showCustomInfoWindow(marker)
            true
        }
    }


    private fun showCustomInfoWindow(marker: Marker) {
        val agence = marker.tag as? Agence
        val gab = marker.tag as? GAB

        val dialogView = LayoutInflater.from(context).inflate(R.layout.marker_info_window, null)
        val tvName: TextView = dialogView.findViewById(R.id.tvAgencyName)
        val tvAddress: TextView = dialogView.findViewById(R.id.tvAddress)
        val tvHours: TextView = dialogView.findViewById(R.id.tvOpeningHours)
        val tvDistance: TextView = dialogView.findViewById(R.id.tvDistance)
        val tvPhone: TextView = dialogView.findViewById(R.id.tvPhone)
        val tvPhone2: TextView = dialogView.findViewById(R.id.tvPhone2)
        val tvFax: TextView = dialogView.findViewById(R.id.tvFax)
        val logo: ImageView = dialogView.findViewById(R.id.type)
        val buttonCall: CardView = dialogView.findViewById(R.id.btnCall)

        if (agence != null) {
            tvName.text = agence.nom
            tvAddress.text = agence.adresse
            tvDistance.text = "Distance: ${agence.distance}"
            tvHours.text = "Horaires d'ouverture: ${agence.horaire2}, Horaires d'été: ${agence.horaire4}, Samedi: ${agence.horaire6}"
            tvPhone.text = agence.telephone1
            tvPhone2.text = agence.telephone2
            tvFax.text = agence.fax

            setDrawableStart(tvPhone, R.drawable.phone)
            setDrawableStart(tvHours, R.drawable.horaire)
            setDrawableStart(tvFax, R.drawable.fax)

            setupButtonListeners(dialogView, agence.telephone1, agence.latitude.toDouble(), agence.longitude.toDouble(), agence.nom, agence.adresse, agence.horaire2)
            if(agence.horaire3.toString().isBlank()){
                logo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.attijari))
            }else{
                logo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.wafacash))
            }

        } else if (gab != null) {
            tvName.text = gab.nom
            tvAddress.text = gab.adresse
            tvDistance.text = "Distance: ${gab.distance}"
            tvHours.visibility = View.GONE
            tvPhone.visibility = View.GONE
            tvPhone2.visibility = View.GONE
            tvFax.visibility = View.GONE
            buttonCall.visibility = View.GONE
            setupButtonListeners(dialogView, "", gab.latitude.toDouble(), gab.longitude.toDouble(), gab.nom, gab.adresse, "")
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .show()
    }


    private fun setDrawableStart(textView: TextView, drawableId: Int) {
        val drawable = ContextCompat.getDrawable(textView.context, drawableId)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }


    private fun setupButtonListeners(
        view: View,
        phoneNumber: String,
        latitude: Double,
        longitude: Double,
        nom: String,
        adresse: String,
        horaire: String
    ) {
        val btnNavigate = view.findViewById<CardView>(R.id.itineraire)
        val btnCall = view.findViewById<CardView>(R.id.btnCall)
        val btnShare = view.findViewById<CardView>(R.id.btnShare)

        btnNavigate.setOnClickListener {
            openNavigation(latitude, longitude)
        }

        btnCall.setOnClickListener {
            dialPhoneNumber(phoneNumber)
        }

        btnShare.setOnClickListener {
            val info = "Agence: $nom\nAdresse: $adresse.\nContact: $phoneNumber.\nHoraires: $horaire"
            shareInformation(info)
        }
    }

    private fun openNavigation(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        googleMapsIntent.setPackage("com.google.android.apps.maps")

        val wazeIntentUri = Uri.parse("waze://?ll=$latitude,$longitude&navigate=yes")
        val wazeIntent = Intent(Intent.ACTION_VIEW, wazeIntentUri)
        wazeIntent.setPackage("com.waze")

        val chooserIntent = Intent.createChooser(googleMapsIntent, "Choisissez une application de navigation")
        val intents = arrayOf(wazeIntent)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)

        startActivity(chooserIntent)
    }




    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun shareInformation(info: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, info)
        startActivity(Intent.createChooser(shareIntent, "Share Via"))
    }


    private fun setDefaultLocation() {
        val defaultLocation = LatLng(33.5850405, -7.6219494)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                setDefaultLocation()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }



}

