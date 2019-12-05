package com.example.a5473locationapp;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private int PERMISSIONS;
    LocationManager locationManager;
    Location wifiLocation;
    Location gpsLocation;
    private LocationListener listener;
    Handler handler;
    LatLng wifiMarker;
    LatLng gpsMarker;
    LatLng fileLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        listener = new LocationListener(){
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(listener);
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        handler = new Handler();

        Button wifi = (Button) findViewById(R.id.wifi_btn);
        Button gps = (Button) findViewById(R.id.gps_btn);
        Button file = (Button) findViewById(R.id.file);
        wifi.setOnClickListener(this);
        gps.setOnClickListener(this);
        file.setOnClickListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSIONS);
        }
        getLocation();
        LatLng gpsMarker = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(gpsMarker).title("GPS Marker"));
        LatLng wifiMarker = new LatLng(wifiLocation.getLatitude(), wifiLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(wifiMarker).title("WiFi Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsMarker));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLocation();
                handler.postDelayed(this, 10000);
            }
        }, 10000);
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        mMap.clear();
        wifiLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        wifiMarker = new LatLng(wifiLocation.getLatitude(), wifiLocation.getLongitude());
        gpsMarker = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(wifiMarker).title("WiFi Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions().position(gpsMarker).title("GPS Marker"));
        if (fileLoc != null) {
            mMap.addMarker(new MarkerOptions().position(fileLoc).title("File Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1000, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1000, listener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gps_btn:
                mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsMarker));
                break;

            case R.id.wifi_btn:
                mMap.moveCamera(CameraUpdateFactory.newLatLng(wifiMarker));
                break;

            case R.id.file:
                fromFile();
                break;
        }
    }

    public void fromFile() {
        String fileName = "location.txt";
        StringBuffer buffer = new StringBuffer();

        InputStream is = this.getResources().openRawResource(R.raw.location);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (is != null) {
            try {
                double lat = Double.parseDouble(reader.readLine());
                double lon = Double.parseDouble(reader.readLine());
                fileLoc = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(fileLoc).title("File Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(fileLoc));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
        }

        try {
            is.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
