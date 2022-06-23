package com.manuni.googlemapu4runtimepermissionusingfragmentxml;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.manuni.googlemapu4runtimepermissionusingfragmentxml.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    ActivityMainBinding binding;
    public static final int PERMISSION_REQUEST_KEY = 9001;
    public static final int LOCATION_REQUEST_KEY = 9003;
    private boolean mLocationPermissionGranted;
    public static final String TAG = "MapDebug";

    HandlerThread handlerThread;

    private FusedLocationProviderClient providerClient;
    private LocationCallback mLocationCallback;

    private final double MOSTAFAPUR_LAT = 23.160979;
    private final double MOSTAFAPUR_LNG = 90.139436;

    GoogleMap mGoogleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Google map");

        initGoogleMap();

        // ------------------------------------------------------------------------------------------------//

        //video 2 is for permission purpose of the FINE_LOCATION
        //video 4 is for dependency adding and also for the meta data in the manifest
        //video 5 is for isServiceOk
        //video 6 is for getting the API key
        //video 7 is for the mapView
        //video 8 is for the map show using fragment in compile time and also using FrameLayout in Runtime
        //video 9 is for the description of the Latitude and Longitude and map 4 properties
        //video 10 is for onMapReady method and also for the use of marker in google map
        //video 11 is for ui properties of maps using xml like uiRotateGestures="true"
        //              map:uiZoomGestures="true"
        //              map:uiTiltGestures="true"
        //              map:uiScrollGestures="true"
        //              map:mapType="satellite"  etc.
        //video 12 is for like video 11 but done with java all these properties
        //video 13 is for different types of Map
        //video 14 is for UI control of google map like zoom button
        //video 15 is for animate camera part see in the block we define there as video 15 to get easily
        //video 16 is for to bound area
        //video 17 is for geocoding with search
        //video 18 is for reverse geocoding
        //video 19 is for checking if the device GPS enabled or disabled
        //video 20 is for getting current location without coding only using one method built in(using my location layer)
        //video 21 is for getting current location of user using java code
        //video 22 is for updating current location(showing in toast and logcat not ui...and also working in Main Thread) as latitude and longitude..in this we need to focus ---> providerClient.requestLocationUpdates(locationRequest,mLocationCallback,null);
        //video 23 is for updating current location realtime showing in the textView and running in the background Thread
        //video 24 is for updating current location in batch and showing also in notification
        //video 25 is for saving current updated location in the shared preferences
        //video 26 is for update current location with started services(basically we used here foreground service) and stop this service using start service and storp service button...pre orio version also compatibility for this..
        //video 27 is for getting update location with pending intent..Intent service is the derived version of started service. Basically it create a thread and handler on the predefined started service ...when we send intent and after doing all the intent work it automatically stop the service

//--------------------------------------------------------------------------------------------------------//

        //below code is map showing with compile time
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        supportMapFragment.getMapAsync(this);

        //video 24
        binding.batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,BatchLocationActivity.class);
                startActivity(intent);
            }
        });

        binding.imageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                geoLocation(view);
            }
        });
    //video 22
     /*   mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null){
                    return;
                }
                Location location = locationResult.getLastLocation();
                Toast.makeText(MainActivity.this, location.getLatitude()+"\n"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onLocationResult: Location is "+location.getLatitude()+"\n"+location.getLongitude());
            }
        };*/

        //video 23
        mLocationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null){
                    return;
                }else {
                    Location location = locationResult.getLastLocation();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                           /* very important thing is...when we try to run in background thread that is involved with the map showing things then it will throw exception...so which line of code is related with the main thread
                            that line of codes must be in the Main thread or UI thread for this reason we put these below codes in the UI Thread...again important thing..how can i realise that these below lind of codes are in the
                            background thread? ans. is if we focus on the "providerClient.requestLocationUpdates(locationRequest,mLocationCallback,handlerThread.getLooper());" line here we can see mLocationCallback available. So
                            it indicates that this callback method is also will run in the background thread...so we need to pass these below line of codes in the UI thread.
                            Again one important thing...Here we used two threads, one is Background Thread and another is Main Thread. Here Background Thread will be execute first then everytime Main Thread will execute his task
                            because we know that threads are run parallel*/
                            binding.locationTxt.setText(location.getLatitude()+ " : "+location.getLongitude());
                            //now we need to move the map camera with this lat and lng value
                            gotoLocation(location.getLatitude(),location.getLongitude());
                            LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
                            showMarker(latLng);
                        }
                    });

                    Log.d(TAG, "onLocationResult: Update Location "+location.getLatitude()+"\n"+location.getLongitude());
                    Log.d(TAG, "onLocationResult: Thread name : "+Thread.currentThread().getName());



                }
            }
        };

        //video 21
        providerClient = LocationServices.getFusedLocationProviderClient(this);

        // below code is map showing with runtime fragment replacing
/*
        GoogleMapOptions googleMapOptions = new GoogleMapOptions().zoomControlsEnabled(true);

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance(googleMapOptions);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,supportMapFragment).commit();

        supportMapFragment.getMapAsync(this);*/

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleMap != null) {

                    //below code is for when start the map it will automatically show with defining zoom value not starting with starting position of the default position
                    // mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(3.0f));


                    //below code is for when start the map it will maintain it's initial position before clicking on the fab

                    //video 15
                   /* mGoogleMap.animateCamera(CameraUpdateFactory.zoomBy(3.0f));
                    LatLng latLng = new LatLng(38.690904,78.051865);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,15);
                    mGoogleMap.animateCamera(update, 5000, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFinish() {
                            Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                        }
                    });*/

                    //below code is for a specific intersect position of an area and make a boundary for that area
                    double bottomBoundaryOfIslamabad = MOSTAFAPUR_LAT - 0.1;
                    double bottomLeftBoundaryOfIslamabad = MOSTAFAPUR_LNG - 0.1;
                    double topBoundaryOfIslamabad = MOSTAFAPUR_LAT + 0.1;
                    double topRightBoundaryOfIslamabad = MOSTAFAPUR_LNG + 0.1;


                    //below code is for a boundary as rectangle area
                    /*double bottomBoundary = 33.662053;
                    double leftBoundary = 72.997233;
                    double topBoundary = 33.743723;
                    double topRightBoundary = 73.087989;
*/

                    LatLngBounds latLngBounds = new LatLngBounds(new LatLng(bottomBoundaryOfIslamabad, bottomLeftBoundaryOfIslamabad), new LatLng(topBoundaryOfIslamabad, topRightBoundaryOfIslamabad));

                    //below code is for going inside the boundary
                   /* mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,1));
                    showMarker(latLngBounds.getCenter());*/

                    //below code is for going the center point of the bound
                   /* mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(),15));
                    showMarker(latLngBounds.getCenter());*/

                    //below code is for showing the bounding area as width and height ... easily to say with zoom in
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 400, 400, 1));
                    showMarker(latLngBounds.getCenter());

                    //below code is for restricting a user to not going over the bound area of latLang. actually you will not be able to go over the boundary by scrolling after clicking on the fab
                    mGoogleMap.setLatLngBoundsForCameraTarget(latLngBounds);


                }
            }
        });


    }

    private void geoLocation(View view) {
        hideSoftKeyboard(view);
        //below code is for showing the user typing location name in the search box
        String locationName = binding.editText.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList.size() > 0) {
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                LatLng addressLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(addressLatLng));
                Toast.makeText(this, address.getLocality(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "geoLocation: " + address.getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //below code is for showing address of multiple not giving any location name in the search box for specific lat lng coordinate value
       /* Geocoder geocoder = new Geocoder(this,Locale.getDefault());

        try {
         List<Address> listAddress = geocoder.getFromLocation(ISLAMABAD_LAT,ISLAMABAD_LNG,3);
         if (listAddress.size()>0){
             gotoLocation(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
             LatLng latLng = new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
             showMarker(latLng);
             Toast.makeText(this, listAddress.get(0).getLocality(), Toast.LENGTH_SHORT).show();
         }
         for (Address address: listAddress){
             Log.d(TAG, "geoLocation: Address: "+address.getAddressLine(address.getMaxAddressLineIndex()));
         }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initGoogleMap() {

        //video 5 for the checking of the google play service is available or not programmatically

        if (isServiceOk()) {
            if (isGPSEnabled()) {

                if (checkSelfPermission()) {
                    Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isServiceOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, PERMISSION_REQUEST_KEY, task ->
                    Toast.makeText(this, "Dialog is cancelled by user", Toast.LENGTH_SHORT).show());

            dialog.show();
        } else {
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }


        return false;

       /* if (mLocationPermissionGranted){
            Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
        }else {
            requestPermission();
        }*/
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_KEY);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_KEY && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission is not granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is starting to show on the screen.");
        mGoogleMap = googleMap;


        gotoLocation(MOSTAFAPUR_LAT, MOSTAFAPUR_LNG); //These are Madaripur Lat, Lng values


        //video 20
        //mGoogleMap.setMyLocationEnabled(true); //we will uncomment when we want to use default system current location service
       /* mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);

*/
        //to do building not showing as 3D
        mGoogleMap.setBuildingsEnabled(false); //by default it is enabled true

        //to get maximum as zoom or minimum then we need to write below code
     /*   mGoogleMap.getMaxZoomLevel();
        mGoogleMap.getMinZoomLevel();*/

      /*  MarkerOptions markerOptions = new MarkerOptions()
                .title("Title")
                .position(new LatLng(0,0));

        mGoogleMap.addMarker(markerOptions);*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.map_type_none:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.map_type_normal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_type_satellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_type_terrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.map_type_hybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.currentLocation:
                //getCurrentLocation();
                getLocationUpdate();
                break;
        }
        return true;
    }
    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        providerClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    Location location = task.getResult();
                    gotoLocation(location.getLatitude(),location.getLongitude());
                }else {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //video 22
    @SuppressLint("MissingPermission")
    private void getLocationUpdate(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        //here this looper is Main Thread looper and for this reason it is working in the Main or UI thread

        //below line of code is only for Main thread
        //providerClient.requestLocationUpdates(locationRequest,mLocationCallback,null);//only we need to focus this line to get all the code line in mind
        //to make a background thread we have to create a background thread using HandlerThread class
        handlerThread = new HandlerThread("thisIsBackgroundThreadName"); // this HandlerThread class contains every necessary things of background Thread there is no need to create separately anything like Handler, looper.prepare, looper.loop
        handlerThread.start();

        //below line of code is for the background thread
        providerClient.requestLocationUpdates(locationRequest,mLocationCallback,handlerThread.getLooper());
        //now we need to stop the HandlerThread because ui thread all time exit automatically after the ending of all the task but background thread need to be stopped explicitly

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handlerThread != null){
            handlerThread.quit();// it will quit all remaining task immediately not for waiting
        }
        //we can also call below method
        //handlerThread.quitSafely(); // it will quit all the remaining task after doing properly
    }

    private void gotoLocation(double lat, double lng){
        LatLng latLng = new LatLng(lat,lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,15);
        mGoogleMap.moveCamera(cameraUpdate);

        //below code is added after writing of codes of menu map type. And for default map.
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        showMarker(latLng);

        //the following way i can find the settings of ui gestures
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
    }
    private void showMarker(LatLng latLng){

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mGoogleMap.addMarker(markerOptions);
    }
    //below method is for enabling the phone GPS
    private boolean isGPSEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled =  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnabled){
            return true;
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permissions")
                    .setMessage("GPS is required for this app. Please enable your phone GPS to continue")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent,LOCATION_REQUEST_KEY);
                        }
                    }).setCancelable(false)
                    .show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_KEY){
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnabled =  manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnabled){
                Toast.makeText(this, "GPS is enabled!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "GPS is not enabled. Unable to show user location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationCallback != null){
            providerClient.removeLocationUpdates(mLocationCallback);
        }
    }
}