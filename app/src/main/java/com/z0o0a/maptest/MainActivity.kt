package com.z0o0a.maptest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.z0o0a.maptest.databinding.ActivityMainBinding
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var myGoogleMap : GoogleMap? = null
    private var myCoo = LatLng(0.0, 0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 구글 맵
        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 현재 위치
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnCurrentPosi.setOnClickListener {
            addMarkToMyCoo()
        }

        binding.btnPolyline.setOnClickListener {
            addPolyline()
        }

        binding.btnCirecle.setOnClickListener {
            addCircle()
        }

        binding.btnPolygon.setOnClickListener {
            addPolygon()
        }

    }

    // 지도 초기 설정
    override fun onMapReady(googleMap: GoogleMap) {
        myGoogleMap = googleMap

        // 좌표
        val seoul = LatLng(37.5666805, 126.9784147)
        // 지도 모양 (인공위성, 노말 등)
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        // 지도 위 마커
        googleMap.addMarker(
            MarkerOptions()
                .position(seoul) // 좌표 설정
                .title("서울 시청") // 마커 이름 설정
                .snippet("대한민국 서울의 시청이다.") // 제목 아래에 표시되는 설명
                .alpha(0.7f) // 마커 투명도
        )
        // 특정 좌표로 카메라 이동
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(seoul))
        // 카메라 줌 (위 코드와 중첩시 이 코드를 우선)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 17f))
    }

    private fun setMyCoo(lat: Double, lon: Double){
        myCoo = LatLng(lat, lon)
    }

    private fun addMarkToMyCoo(){
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        setMyCoo(location.latitude, location.longitude)

                        try {
                            val list = Geocoder(this).getFromLocation(myCoo.latitude, myCoo.longitude, 1)

                            // 현재 위치 마커 생성
                            myGoogleMap!!.addMarker(
                                MarkerOptions()
                                    .position(myCoo)
                                    .title("${list[0].adminArea} ${list[0].thoroughfare} ${list[0].postalCode}")
                            )!!.showInfoWindow()
                            // 현재 위치로 카메라 이동
                            myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoo, 17f))
                        }catch (e: IOException){
                            Toast.makeText(this, "주소 가져오기 실패", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }else if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }else{
            Toast.makeText(this, "현재 위치 불러오기 실패", Toast.LENGTH_LONG).show()
        }
    }

    private fun addPolyline(){
        myGoogleMap!!.addPolyline(
            PolylineOptions()
                .add(LatLng(35.1542634, 129.1204897))
                .add(LatLng(37.5666805, 126.9784147))
                .add(LatLng(34.7603737, 127.6622221))
                .add(LatLng(35.1542634, 129.1204897))
        ).color = Color.parseColor("#ff0000")

        myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(36.0, 128.0), 6f))
    }

    private fun addCircle(){
        myGoogleMap!!.addCircle(
            CircleOptions()
                .center(LatLng(35.1542634, 129.1204897))
                .radius(10000.0)
        ).strokeColor = Color.parseColor("#0000ff")

        myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.1542634, 129.1204897), 8f))
    }

    private fun addPolygon(){

    }

}