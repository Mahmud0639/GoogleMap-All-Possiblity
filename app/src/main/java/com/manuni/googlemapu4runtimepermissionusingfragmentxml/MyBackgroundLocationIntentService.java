package com.manuni.googlemapu4runtimepermissionusingfragmentxml;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

//video 27
public class MyBackgroundLocationIntentService extends IntentService {
    public static final String ACTION_PROCESS_UPDATE = "com.manuni.googlemapu4runtimepermissionusingfragmentxml"+".PROCESS_UPDATES";
    private static final String TAG = "MyTag";


    public MyBackgroundLocationIntentService() {
        super("MyBackgroundLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: called.");
        if (intent != null) {
            if (ACTION_PROCESS_UPDATE.equals(intent.getAction())){  //ei intent jekhan theke ashce oi page er setAction a set kora action er sathe ei page a receive kora intent er sathe jodi mil paoya jay tahole ki korbo ta bujhasse...
                LocationResult locationResult =  LocationResult.extractResult(intent);

                //intent er maddhome pass kora locations gulo extract korar jonno use kora hoice LocationResult.extractResult method..ar ei locationResult ager use kora locationResult er moto same(BatchLocationActivity class er LocationCallBack interface er moddhe use kora locationResult)
                if (locationResult != null){
                    List<Location> locations = locationResult.getLocations();

                    LocationResultHelper locationResultHelper = new LocationResultHelper(getApplicationContext(), locations);
                    locationResultHelper.saveLocation();
                    locationResultHelper.showNotification();
                    BatchLocationActivity activity = new BatchLocationActivity();
                    activity.binding.locationResult.setText(locationResultHelper.getLocationText());
                }
            }




        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
    }
}