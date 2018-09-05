package com.supportaeon.supportaeonapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ArqTras on 05/09/2018.
 */

public class CheckDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences("SupportAeonAppKeys", context.MODE_PRIVATE);



        String userwallet = prefs.getString("WalletAddress", "");

        networkStats();


        if(userwallet.length() > 0)
            userStats(userwallet);

        Boolean notificationBlock = prefs.getBoolean("NotificationBlock", false);

        if(notificationBlock)
            checkNewBlock(context, prefs);

        Boolean PaymentReceived = prefs.getBoolean("PaymentReceived", false);

        if(PaymentReceived)
        {
            checkPaymentReceived(context, prefs);

        }

        Boolean BlockUnlocked = prefs.getBoolean("BlockUnlocked", false);

        if(BlockUnlocked)
        {
            checkBlocksMaturated(context, prefs);

        }

        Boolean statusNotification = prefs.getBoolean("statusNotification", false);

        if(statusNotification) {
            ongoingNotification(context, prefs);
        }
    }


    private void checkBlocksMaturated(final Context context, final SharedPreferences prefs)
    {
        String[] parms = { ConnectionHandler.baseSiteUrl + "pool/blocks/pplns"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {

                    int countBlocks = 0;

                    String OldBlocksStr = prefs.getString("OldBlocks", "[]");

                    if(OldBlocksStr.length() > 0) {

                        JSONArray NewBlocks = new JSONArray(str);

                        JSONArray OldBlocks = new JSONArray(OldBlocksStr);

                        for (int i = 0; i < NewBlocks.length(); i++) {
                            JSONObject newBlock = NewBlocks.getJSONObject(i);

                            for (int j = 0; j < OldBlocks.length(); j++) {
                                JSONObject oldBlock = OldBlocks.getJSONObject(j);

                                if (newBlock.getString("hash").compareTo(oldBlock.getString("hash")) == 0) {
                                    if (newBlock.getBoolean("unlocked") != oldBlock.getBoolean("unlocked")) {

                                        countBlocks++;


                                    }

                                }

                            }

                        }

                        if (countBlocks > 0) {

                            String restoredWallet = prefs.getString("WalletAddress", "");

                            String text = "Set your wallet in app to see user stats";
                            if (restoredWallet.length() > 0) {


                                if(localUserValues != null)
                                {
                                    Double amD = localUserValues.getAmountDue();
                                    Double amP = localUserValues.getAmountPayed();

                                    DecimalFormat df = new DecimalFormat("0.000");

                                    text = "Amount due: " + df.format(amD) + "  paid: " + df.format(amP);

                                }





                            }

                            if (countBlocks == 1) {
                                sendNotification(2, "1 Block Unlocked", text, context, false, R.drawable.ic_launcher);
                            } else {
                                sendNotification(2, countBlocks + " Blocks Unlocked", text, context, false, R.drawable.ic_launcher);
                            }

                        }
                    }

                    SharedPreferences.Editor editor = context.getSharedPreferences("SupportAeonAppKeys", context.MODE_PRIVATE).edit();
                    editor.remove("OldBlocks");
                    editor.putString("OldBlocks", str);
                    editor.apply();





                }
                catch (Exception ex)
                {

                }


            }
        });


        ch.execute(parms);

    }

    private void  checkPaymentReceived(final Context context, final SharedPreferences prefs)
    {

        String restoredWallet = prefs.getString("WalletAddress", "");

        if(restoredWallet.length() == 0)
            return;

        String[] parms = { ConnectionHandler.baseSiteUrl + "miner/" + restoredWallet + "/payments"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {




                    JSONArray newPayments = new JSONArray(str);

                    String lastPaymentsFoundString = prefs.getString("lastPaymentsFound", "");

                    if(lastPaymentsFoundString.length() > 0) {

                        JSONArray oldPayments = new JSONArray(lastPaymentsFoundString);

                        int totalPaymentsFound = 0;
                        double totalAmount = 0;

                        int newPaymentsSize = newPayments.length();
                        if (newPaymentsSize > 10)
                            newPaymentsSize = 10;

                        int oldPaymentsSize = oldPayments.length();
                        if (oldPaymentsSize > 10)
                            oldPaymentsSize = 10;

                        for (int i = 0; i < newPaymentsSize; i++) {
                            JSONObject newPayment = newPayments.getJSONObject(i);

                            boolean found = false;

                            String newtxnHash = newPayment.getString("txnHash");

                            for (int j = 0; j < oldPaymentsSize; j++) {
                                JSONObject oldPayment = oldPayments.getJSONObject(j);

                                String oldtxnHash = oldPayment.getString("txnHash");

                                if (newtxnHash.compareTo(oldtxnHash) == 0)
                                    found = true;
                            }

                            if (!found) {
                                totalAmount += newPayment.getDouble("amount");
                                totalPaymentsFound++;
                            }

                        }


                        Double divider = Double.parseDouble("1000000000000");

                        if (totalPaymentsFound > 0) {
                            if (totalPaymentsFound == 1) {
                                sendNotification(1, "1 New Payment Received", "Amount: " + (totalAmount / divider) + " ARQMA", context, false, R.mipmap.ic_launcher);
                            } else {
                                sendNotification(1, totalPaymentsFound + " New Payments Received", "Amount: " + (totalAmount / divider) + " ARQMA", context, false, R.mipmap.ic_launcher);
                            }

                        }

                    }

                    SharedPreferences.Editor editor = context.getSharedPreferences("SupportAeonAppKeys", context.MODE_PRIVATE).edit();
                    editor.remove("lastPaymentsFound");
                    editor.putString("lastPaymentsFound", str);
                    editor.apply();

                }
                catch (Exception ex)
                {

                }


            }
        });


        ch.execute(parms);

    }


    private void checkNewBlock(final Context context, final SharedPreferences prefs)
    {




        String[] parms = { ConnectionHandler.baseSiteUrl + "pool/blocks/pplns"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {



                    JSONArray newBlocks = new JSONArray(str);

                    String lastBlocksfoundStr = prefs.getString("lastBlocksFound", "");

                    if(lastBlocksfoundStr.length() > 0) {

                        JSONArray oldBlocks = new JSONArray(lastBlocksfoundStr);

                        int totalNewBlocks = 0;


                        for (int i = 0; i < newBlocks.length(); i++) {
                            JSONObject newBlock = newBlocks.getJSONObject(i);

                            boolean found = false;

                            String newHash = newBlock.getString("hash");

                            for (int j = 0; j < oldBlocks.length(); j++) {
                                JSONObject oldBlock = oldBlocks.getJSONObject(j);

                                String oldHash = oldBlock.getString("hash");

                                if (newHash.compareTo(oldHash) == 0)
                                    found = true;
                            }

                            if (!found) {
                                totalNewBlocks++;
                            }

                        }


                        if (totalNewBlocks > 0) {


                            if (totalNewBlocks == 1) {
                                sendNotification(1, "New Block", "One new block was found", context, false, R.mipmap.ic_launcher);
                            } else {
                                sendNotification(1, totalNewBlocks + " New Blocks", totalNewBlocks + " new blocks were found", context, false, R.mipmap.ic_launcher);
                            }

                        }

                    }

                    SharedPreferences.Editor editor = context.getSharedPreferences("SupportAeonAppKeys", context.MODE_PRIVATE).edit();
                    editor.remove("lastBlocksFound");
                    editor.putString("lastBlocksFound", str);
                    editor.apply();

                }
                catch (Exception ex)
                {

                }


            }
        });


        ch.execute(parms);

    }

    public UserValues localUserValues = null;

    public void userStats(String wallet)
    {
        String[] parms = { ConnectionHandler.baseSiteUrl + "miner/" + wallet + "/Stats"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {

                    Double divider = Double.parseDouble("1000000000000");

                    JSONObject jsonValues = new JSONObject(str);

                    UserValues uservalues = new UserValues();

                    uservalues.amountDue = (jsonValues.getDouble("amtDue") / divider);

                    uservalues.amountPayed = (jsonValues.getDouble("amtPaid") / divider);

                    uservalues.hash = (jsonValues.getDouble("hash") / 1000);

                    localUserValues = uservalues;
                }
                catch (Exception ex)
                {

                }



            }
        });


        ch.execute(parms);



    }

    public Double networkDifficulty = 0.0;

    public void networkStats()
    {
        String[] parms = { ConnectionHandler.baseSiteUrl + "network/stats"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {



                    JSONObject jsonValues = new JSONObject(str);

                    networkDifficulty = jsonValues.getDouble("difficulty");

                }
                catch (Exception ex)
                {

                }



            }
        });


        ch.execute(parms);



    }

    public void ongoingNotification(final Context context, final SharedPreferences prefs)
    {

        String[] parms = { ConnectionHandler.baseSiteUrl + "pool/stats/pplns"};

        ConnectionHandler ch = new ConnectionHandler();

        ch.setOnResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String str) {


                try {

                    Double divider = Double.parseDouble("1000000000000");

                    JSONObject jsonValuesStart = new JSONObject(str);

                    JSONObject jsonValues = jsonValuesStart.getJSONObject("pool_statistics");

                    double hashRate = (jsonValues.getDouble("hashRate") / 1000);

                    //int miners = jsonValues.getInt("miners");
                    //{{((poolStats.global.roundHashes / network.difficulty)*100).toFixed(1) | number}} % Current Effort

                    double roundHashes = jsonValues.getDouble("roundHashes");

                    double effort = (roundHashes / networkDifficulty)*100;

                    String lastBlockFoundTime = jsonValues.getString("lastBlockFoundTime");


                    DecimalFormat df = new DecimalFormat("0");
                    DecimalFormat df1 = new DecimalFormat("0.0");
                    DecimalFormat df2 = new DecimalFormat("0.00");
                    DecimalFormat df3 = new DecimalFormat("0.000");


                    /*
                    Date currentDate = new Date(System.currentTimeMillis());

                    Date blockDate = new java.util.Date(Long.parseLong(lastBlockFoundTime));

                    long elapsedTimeMillis = currentDate.getTime() - blockDate.getTime();

                    float elapsedTimeMin = elapsedTimeMillis/(60*1000);

                    String timelastBlock = "";


                    if(elapsedTimeMin < 60)
                    {
                        timelastBlock =  df.format(elapsedTimeMin) + "min";

                    }
                    else{
                        timelastBlock = df.format(elapsedTimeMin/60) + "h";
                    }
                    */

                    String poolText = "Pool: Hash:" + df.format(hashRate) + "KH/s  Effort:" + df.format(effort) + "% "; //  Last:" + timelastBlock;

                    String userText = "";
                    if(localUserValues != null)
                    {
                        Double amD = localUserValues.getAmountDue();
                        Double amP = localUserValues.getAmountPayed();
                        Double hash = localUserValues.getHash();



                        userText = "You: Hash:" +  df2.format(hash) + "KH/s due:" + df2.format(amD) + " paid:" + df2.format(amP);

                    }

                    sendNotification(4, poolText, userText, context, true, R.mipmap.ic_launcher);

                }
                catch (Exception ex)
                {

                }



            }
        });


        ch.execute(parms);


    }


    private void sendNotification(int id, String title, String msg, Context context, boolean ongoing, int icon)
    {

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

        mBuilder.setSmallIcon(icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(msg);

        if(ongoing){
            mBuilder.setOngoing(true);
            mBuilder.setAutoCancel(false);
            mBuilder.setShowWhen(false);
            //mBuilder.setLargeIcon(R.mipmap.ic_launcher_round)
            int color = context.getResources().getColor(R.color.colorPrimary);
            mBuilder.setColor(color);

            /*
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setTextSize(16);
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.MONOSPACE);
            */
        }
        else
        {
            mBuilder.setSound(alarmSound);
            mBuilder.setVibrate(new long[] { 1000, 1000 });
            mBuilder.setLights(Color.RED, 3000, 3000);
            mBuilder.setAutoCancel(true);
        }

// Creates an explicit intent for an Activity in your app

        Intent returnIntent = new Intent(context, MainActivity.class);

        Intent resultIntent = new Intent(context, MainActivity.class);

        //context.startActivity(returnIntent);


// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        //stackBuilder.addParentStack(LoginActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        //stackBuilder.addNextIntent(resultIntent);
        /*
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );*/


        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());

    }



}


