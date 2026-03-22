package com.ln.painel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private var lat = 0.0
    private var lng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        val pos = LatLng(lat, lng)
        map.addMarker(MarkerOptions().position(pos).title("Dispositivo"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17f))
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
    }
}
