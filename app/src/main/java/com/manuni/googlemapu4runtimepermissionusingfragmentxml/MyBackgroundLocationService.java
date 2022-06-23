package com.manuni.googlemapu4runtimepermissionusingfragmentxml;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

//video 26(started service)
public class MyBackgroundLocationService extends Service {
    private FusedLocationProviderClient providerClient;
    private LocationCallback mLocationCallback;
    private static final String TAG = MyBackgroundLocationService.class.getSimpleName(); //it will get class name

    public MyBackgroundLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        providerClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mLocationCallback =  new LocationCallback() {
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
                LocationResultHelper helper = new LocationResultHelper(MyBackgroundLocationService.this,location);

                //ekhane amra iccha korle Firebase a data pass kore save korte pari , also Room database a save korte pari ekhane just amra data gulo shared preference a save koresi
                helper.showNotification();
                //video 25
                helper.saveLocation();
                Toast.makeText(MyBackgroundLocationService.this, "Location received "+location.size(), Toast.LENGTH_SHORT).show();

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        //ekhane forground service use korar karon holo..jodi amra app ke ram theke bad diye dei tahole app er background service kichu somoy pore again start houyar kotha but
        //orio api level er khetre system nije thekei eta ke stop kore dei again start korte badha dey. tai ei somossa theke mukti paoyar jonno 2 ta upay ache..1. holo job scheduler 2. holo startForeground
        startForeground(1001,getNotification());
        getLocationUpdate();
        return START_STICKY; // app ke ram theke remove kore dileo service chalu thakbe but new intent ashbe na
    }

    private Notification getNotification() {
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),App.CHANNEL_ID)
                .setContentTitle("Location Notification")
                .setContentText("Notification service is running in the Background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        return notificationBuilder.build(); // system nijei access korbe je method access kora lagbe.
    }

    private void getLocationUpdate(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // 2 sec. er moddhe jodi kono location receive na kore tahole system nije 5 sec. por request korbe
        locationRequest.setFastestInterval(4000); // ekhane 2 sec. por por location receive korar kotha bola ache
        locationRequest.setMaxWaitTime(15 * 1000); // ekhane 15 sec. wait korbe...er vitore jodi kono update na ashe tahole ja receive hoice ta show korbe

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        providerClient.requestLocationUpdates(locationRequest,mLocationCallback, Looper.myLooper()); // eta main thread er looper ke use korbe
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
        //video 26(only below one line)
        stopForeground(true); // ekhane false pass korle notification remove kora jeto na tai je notification ta remove kora jay na ota remove korar jonno ekhane true pass korte hobe
        providerClient.removeLocationUpdates(mLocationCallback); // jehetu amra location request koresi tai eta remove o korte hobe...jodi remove na kora hoy tahole stop service call korar poreo eta location er update nite thakbe
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}