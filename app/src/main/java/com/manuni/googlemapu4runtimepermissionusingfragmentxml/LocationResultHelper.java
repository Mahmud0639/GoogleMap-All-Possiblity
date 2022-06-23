package com.manuni.googlemapu4runtimepermissionusingfragmentxml;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
//video 24
public class LocationResultHelper {
    public static final String KEY_LOCATION_REQ = "key-location-request";
    private Context context;
    private List<Location> locations;
    public static final String KEY_LOCATION_RESULT = "key-location-result";




    public LocationResultHelper(Context context, List<Location> locations){
        this.context = context;
        this.locations = locations;
    }
//video 27 for button enabled or disabled

    public static void setLocationRequestStatus(Context context, boolean value){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_LOCATION_REQ,value).apply();
    }
    public static boolean getLocationRequestStatus(Context context) {
      return   PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_LOCATION_REQ,false);

    }


    public String getLocationText(){
        if (locations.isEmpty()){
            return "Location not received";
        }else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Location location: locations){
                stringBuilder.append("(");
                stringBuilder.append(location.getLatitude());
                stringBuilder.append(",");
                stringBuilder.append(location.getLongitude());
                stringBuilder.append(")");
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }
    }
    private CharSequence getLocationTitle(){
        String result = context.getResources().getQuantityString(R.plurals.num_location_reported,locations.size(),locations.size());
        return result +" : "+ DateFormat.getDateInstance().format(new Date());
    }

    public void showNotification(){
        Intent notificationIntent = new Intent(context,BatchLocationActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(context,App.CHANNEL_ID)
                    .setContentTitle(getLocationTitle())
                    .setContentText(getLocationText())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0,notificationBuilder.build());

    }

    private NotificationManager getNotificationManager(){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    //video 25
    public void saveLocation(){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_RESULT,getLocationTitle()+"\n"+getLocationText()).apply();
    }
    public static String getSavedLocationData(Context mContext){
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(KEY_LOCATION_RESULT,"");
    }


}
