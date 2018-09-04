package com.supportaeon.supportaeonapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by romeu on 15/01/2018.
 */

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ){



            iniciarNotifications(context);
        }


    }

    protected void iniciarNotifications(Context context)
    {


        Intent resultIntent = new Intent(context, CheckDataReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                resultIntent,
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Toast.makeText(context, "ArQmA.com server check already running", Toast.LENGTH_LONG).show();
        }
        else
        {
            SharedPreferences prefs = context.getSharedPreferences("SupportAeonAppKeys", context.MODE_PRIVATE);

            int minutes = prefs.getInt("updateInterval", 0);

            int interval = 60000;
            String txt = "1m";
            switch (minutes)
            {
                case 0:  interval = 60 * 1000;
                    txt = "1m";
                    break;
                case 1:  interval = 60 * 1000 * 5;
                    txt = "5m";
                    break;
                case 2:  interval = 60 * 1000 * 15;
                    txt = "15m";
                    break;
                case 3:  interval = 60 * 1000 * 30;
                    txt = "30m";
                    break;
                case 4:  interval = 60 * 1000 * 60;
                    txt = "1h";
                    break;
                case 5:  interval = 60 * 1000 * 60 * 6;
                    txt = "6h";
                    break;
                case 6:  interval = 60 * 1000 * 60 * 12;
                    txt = "12h";
                    break;
            }

            AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, resultIntent, 0);

            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

            Toast.makeText(context, "ArQmA.com server check started every " +  txt, Toast.LENGTH_LONG).show();
        }




    }
}
