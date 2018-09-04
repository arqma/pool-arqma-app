package com.supportaeon.supportaeonapp;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by romeu on 15/01/2018.
 */

public class ConnectionHandler extends AsyncTask<String, Void, String> {
    //public static String baseSiteUrl = "http://localhost:26575/mobile/";
    public static String baseSiteUrl = "https://api.supportaeon.com/";

    ArrayList<ResponseListener> listeners = new ArrayList<ResponseListener> ();

    private static String getContentFromServer(String path)
    {


        String tempString = "";

        try
        {
            URL url = new URL(path);
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null)
            {

                tempString = tempString + str;
            }
            in.close();
        } catch (Exception e) {
            return "false";
        }

        return tempString;
    }


    @Override
    protected String doInBackground(String... params) {
        return getContentFromServer(params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        for (ResponseListener listener : listeners)
        {
            listener.onResponse(result);
        }
    }

    public void setOnResponseListener (ResponseListener listener)
    {
        this.listeners.add(listener);
    }
}
