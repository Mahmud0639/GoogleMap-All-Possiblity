package com.manuni.googlemapu4runtimepermissionusingfragmentxml;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.manuni.googlemapu4runtimepermissionusingfragmentxml.databinding.ActivityBatchLocationBinding;

import java.util.List;

public class BatchLocationActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    ActivityBatchLocationBinding binding;
    private FusedLocationProviderClient providerClient;
    private LocationCallback mLocationCallback;
    public static final String TAG = "MyTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBatchLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        providerClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    Log.d(TAG, "onLocationResult: locationError");
                    return;
                }
                //below code is for when we want to show only in the text view
               /* Location location = locationResult.getLastLocation();
                Toast.makeText(BatchLocationActivity.this, location.getLatitude() + "\n" + location.getLongitude(), Toast.LENGTH_SHORT).show();
                binding.locationResult.setText(location.getLatitude() + " : " + location.getLongitude());*/

                List<Location> location = locationResult.getLocations();
                LocationResultHelper helper = new LocationResultHelper(BatchLocationActivity.this,location);
                helper.showNotification();
                //video 25
                helper.saveLocation();
                Toast.makeText(BatchLocationActivity.this, "Location received "+location.size(), Toast.LENGTH_SHORT).show();
                binding.locationResult.setText(helper.getLocationText());
            }
        };
        binding.requestUpdateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(5000); // 2 sec. er moddhe jodi kono location receive na kore tahole system nije 5 sec. por request korbe
                locationRequest.setFastestInterval(4000); // ekhane 2 sec. por por location receive korar kotha bola ache
                locationRequest.setMaxWaitTime(15 * 1000); // ekhane 15 sec. wait korbe...er vitore jodi kono update na ashe tahole ja receive hoice ta show korbe

                if (ContextCompat.checkSelfPermission(BatchLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                providerClient.requestLocationUpdates(locationRequest, mLocationCallback, null);//ekhane looper null thakar karone eti UI thread er looper use hocce..mane eta er default looper ke use korbe
            }
        });

        //below code is for video 27
      /*  binding.requestBatchLocationBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                requestUpdatesForBatchLocation();
            }
        });*/

        binding.startLocationServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //below code is for only started services
                Intent intent = new Intent(BatchLocationActivity.this,MyBackgroundLocationService.class);
                ContextCompat.startForegroundService(BatchLocationActivity.this,intent); // we need to use foreground service because we want to use this service as foreground not only background because API orio automatically stopped the service after sometimes
                Toast.makeText(BatchLocationActivity.this, "Service started.", Toast.LENGTH_SHORT).show();

                //below method calling is for intent service
                //requestUpdatesForBatchLocation();
               // LocationResultHelper.setLocationRequestStatus(BatchLocationActivity.this,true);

            }
        });
        binding.stopLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stopIntent = new Intent(BatchLocationActivity.this,MyBackgroundLocationService.class);
                stopService(stopIntent);
                Toast.makeText(BatchLocationActivity.this, "Service stopped.", Toast.LENGTH_SHORT).show();
               // providerClient.removeLocationUpdates(getPendingIntent());
               // LocationResultHelper.setLocationRequestStatus(getApplicationContext(),false);
            }
        });

    }


  /*  private void requestUpdatesForBatchLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 2 sec. er moddhe jodi kono location receive na kore tahole system nije 5 sec. por request korbe
        locationRequest.setFastestInterval(4000); // ekhane 2 sec. por por location receive korar kotha bola ache
        locationRequest.setMaxWaitTime(15 * 1000); // ekhane 15 sec. wait korbe...er vitore jodi kono update na ashe tahole ja receive hoice ta show korbe

        if (ContextCompat.checkSelfPermission(BatchLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        providerClient.requestLocationUpdates(locationRequest, getPendingIntent());

        //ekhane jokhoni request update ashbe tokhoni sob kichu pending intent er moddhe wrap hoye chole jabe. Ar ei getPending intent er maddhome Intent er maddhome chole jabe
        //MyBackgroundLocationIntentService er class a jar fole intent service chalu hobe ar ei intent takei return kore pathano hobe ekhane
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        providerClient.removeLocationUpdates(mLocationCallback);
    }
//video 25
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //jokhoni shared preference a kono prokar change ashbe means kono value save hobe sathe sathe onSharedPreferenceChanged method ti call hobe
        if (s.equals(LocationResultHelper.KEY_LOCATION_RESULT)){
            binding.locationResult.setText(LocationResultHelper.getSavedLocationData(this));
        }/*else if (s.equals(LocationResultHelper.KEY_LOCATION_REQ)){//video 27
            setButtonEnabledState(LocationResultHelper.getLocationRequestStatus(getApplicationContext()));

        }*/
    }

    //now we need to register this shared preference

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    // when app is stopped then we need to unregister this shared preference

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.locationResult.setText(LocationResultHelper.getSavedLocationData(this));
        //setButtonEnabledState(LocationResultHelper.getLocationRequestStatus(this)); //video 27
    }
    private PendingIntent getPendingIntent(){
        //pending intent basically wrap kore normal intent ke.
        Intent intent = new Intent(getApplicationContext(),MyBackgroundLocationIntentService.class);
        intent.setAction(MyBackgroundLocationIntentService.ACTION_PROCESS_UPDATE); // eta deoyar maddhome PendingIntent bujhte parbe amra kon service use kortesi,,intent service naki only started service
       return PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private void setButtonEnabledState(boolean value){
        if (value){
            binding.startLocationServiceBtn.setEnabled(false);
            binding.stopLocationService.setEnabled(true);
        }else {
            binding.startLocationServiceBtn.setEnabled(true);
            binding.stopLocationService.setEnabled(false);
        }
        //LocationResultHelper.getLocationRequestStatus(this);
    }
}