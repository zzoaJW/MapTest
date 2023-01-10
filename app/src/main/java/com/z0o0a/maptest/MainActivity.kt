package com.z0o0a.maptest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.LongDef
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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

    private var moveCooList : MutableList<LatLng> = mutableListOf()
    private val handler = Handler(Looper.getMainLooper())
    private var firStart = false
    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 구글 맵
        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 현재 위치 불러오기위한 권한 요청
        checkLocationPermission()

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

        binding.btnAllClear.setOnClickListener {
            myGoogleMap!!.clear()
        }

        binding.btnZoomIn.setOnClickListener {
            myGoogleMap!!.moveCamera(CameraUpdateFactory.zoomIn())
        }

        binding.btnZoomOut.setOnClickListener {
            myGoogleMap!!.moveCamera(CameraUpdateFactory.zoomOut())
        }

        binding.btnStart.setOnClickListener {
            routeStart()
        }

        binding.btnPause.setOnClickListener {
            routePause()
        }

        binding.btnStop.setOnClickListener {
            routeStop()
        }

    }

    // 현재 위치 불러오기 위한 권한 요청
    private fun checkLocationPermission(){
        when {
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
            }
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED -> {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
            }
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
        // 카메라 줌 (위 코드와 중첩시 이 코드를 우선)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 17f))
    }

    private fun setMyCoo(lat: Double, lon: Double){
        myCoo = LatLng(lat, lon)
    }

    private fun addMarkToMyCoo(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
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
        )
//            .fillColor = Color.parseColor("#0000ff")
//            .strokeColor = Color.parseColor("#0000ff")

        myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.1542634, 129.1204897), 8f))
    }

    private fun addPolygon(){
        myGoogleMap!!.addPolygon(
            PolygonOptions()
                .add(
                    LatLng(35.1542634, 129.1204897),
                    LatLng(34.7603737, 127.6622221),
                    LatLng(37.5666805, 126.9784147),
                    LatLng(37.8813153, 127.7299707),
                    LatLng(35.1542634, 129.1204897)
                )
                .strokeColor(Color.GREEN)
                .fillColor(Color.LTGRAY)
        )
        myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.1542634, 129.1204897), 8f))
    }

    private fun routeStart(){
        if(!firStart){
            Toast.makeText(this, "경로 저장을 시작합니다.", Toast.LENGTH_LONG).show()
            firStart = true

            // moveCooList에 최초 위치 저장
            // 최초 위치로 출발지점 마커 생성 (파란 동그라미)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // moveCooList : MutableList<LatLng> 에 최초 위치 저장
                        moveCooList += LatLng(location.latitude, location.longitude)

                        // 최초 위치로 출발지점 마커 생성 (파란 동그라미)
                        myGoogleMap!!.addMarker(
                            MarkerOptions()
                                .position(moveCooList[0])
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_blue))
                        )

                        // 최초 위치로 카메라 이동
                        myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCooList[0], 17f))
                    }
                }
        }

        // 3초마다 현재 위치의 좌표를 moveCooList에 저장
        // 저장하면 i-1번째 좌표, i번째 좌표로 add polyline
        handler.post(object: Runnable{
            override fun run(){
                // 현재 위치의 좌표를 moveCooList에 저장
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            // moveCooList : MutableList<LatLng> 에 최초 위치 저장
                            moveCooList += LatLng(location.latitude, location.longitude)

                            i += 1
                            // 저장하면 i-1번째 좌표, i번째 좌표로 add polyline
                            myGoogleMap!!.addPolyline(
                                PolylineOptions()
                                    .add(moveCooList[i-1])
                                    .add(moveCooList[i])
                            ).color = Color.parseColor("#515151")
                        }
                    }

                // 3초마다 반복
                handler.postDelayed(this, 3000)
            }
        })
    }

    private fun routePause(){
        // 일시정지 (3초마다 현재 위치의 좌표를 moveCooList에 저장했던거 정지)
        handler.removeCallbacksAndMessages(null)
    }

    private fun routeStop(){
        Toast.makeText(this, "경로 저장을 종료합니다.", Toast.LENGTH_LONG).show()
        firStart = false

        // 3초마다 현재 위치의 좌표를 moveCooList에 저장했던거 정지
        handler.removeCallbacksAndMessages(null)

        // 현재 위치에 빨간 마커
        // add polyline
        // 현재 위치 moveCooList에 저장
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // moveCooList에 마지막 위치 저장
                    moveCooList += LatLng(location.latitude, location.longitude)

                    // 저장하면 마지막 직전 좌표, 마지막 좌표로 add polyline
                    myGoogleMap!!.addPolyline(
                        PolylineOptions()
                            .add(moveCooList[moveCooList.lastIndex-1])
                            .add(moveCooList[moveCooList.lastIndex])
                    ).color = Color.parseColor("#515151")

                    // 최초 위치로 출발지점 마커 생성 (빨간 동그라미)
                    myGoogleMap!!.addMarker(
                        MarkerOptions()
                            .position(moveCooList[moveCooList.lastIndex])
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_red))
                    )

                    // 마지막 위치로 카메라 이동
                    myGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCooList[moveCooList.lastIndex], 17f))
                }
            }

        // moveCooList 저장


        // moveCooList 비우기
        moveCooList.clear()
    }
}