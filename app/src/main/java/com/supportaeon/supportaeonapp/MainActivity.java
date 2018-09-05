package com.supportaeon.supportaeonapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onResume()
    {
        super.onResume();

        TextView textViewError = (TextView) findViewById(R.id.textViewError);
        textViewError.setVisibility(View.INVISIBLE);
        RelativeLayout LayoutLoading = (RelativeLayout) findViewById(R.id.LayoutLoading);
        LayoutLoading.setVisibility(View.VISIBLE);
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.VISIBLE);

        errorFound = false;

        WebView myWebView = (WebView) findViewById(R.id.myWebView);
        //loadPage(this);
        myWebView.reload();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        if (progressBarLoading != null) {
            progressBarLoading.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFA000"), android.graphics.PorterDuff.Mode.MULTIPLY);
        }*/

        if(savedInstanceState == null)
            iniciarNotifications(this);

        loadPage(this);



    }


    public  boolean isPageLoad = true;
    public boolean errorFound = false;

    protected void loadPage(final Context context)
    {


        TextView textViewError = (TextView) findViewById(R.id.textViewError);
        textViewError.setVisibility(View.INVISIBLE);

        ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(View.VISIBLE);

        errorFound = false;

        final WebView myWebView = (WebView) findViewById(R.id.myWebView);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);



        myWebView.setWebViewClient(new WebViewClient(){



            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
            {

                errorFound = true;

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                String url2="https://pool.arqma.com/";
                // all links  with in ur site will be open inside the webview
                //links that start ur domain example(http://www.example.com/)
                if (url != null && url.startsWith(url2)){
                    return false;
                }
                // all links that points outside the site will be open in a normal android browser
                else  {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }

                //view.loadUrl(url);
                //return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);


                String walletMsg = "Set your wallet address in settings to see your statistics";


                SharedPreferences prefs = getSharedPreferences("SupportAeonAppKeys", MODE_PRIVATE);
                final String restoredWallet = prefs.getString("WalletAddress", "");

                if(restoredWallet.length() != 0){
                    walletMsg = "";
                }else
                {
                    Toast.makeText(context, walletMsg, Toast.LENGTH_LONG).show();

                }



                myWebView.loadUrl(
                        "javascript:(function() { " +

                                "angular.element(document.getElementsByTagName('button')[2]).scope().paymentAddress='" + restoredWallet + "'; " +
                                "angular.element(document.getElementsByTagName('button')[2]).scope().addAddress();" +
                                "document.getElementsByTagName('form')[0].innerHTML = '" + walletMsg + "';" +
                                "document.getElementsByClassName('footer')[0].innerHTML += '<br/>Android App By <a href=\"https://arqma.com.com\" target=\"_blank\">arqma.com</a>'; "+
                                "var buttonArray = document.getElementsByTagName('button');" +
                                "var ids = [];" +
                                "for(i = 0; i < buttonArray.length; i++){ " +
                                " if(buttonArray[i].innerText != 'VIEW PAYMENTS') { ids[i] = buttonArray[i].id = 'buttonD' + i; }" +
                                "} " +
                                "for(i = 0; i < ids.length; i++){ var elem = document.getElementById(ids[i]); elem.remove(); }" +
                                "})()");


                SystemClock.sleep(1000);

                myWebView.loadUrl(
                        "javascript:(function() { " +


                                "var buttonArray = document.getElementsByTagName('button');" +
                                "var ids = [];" +
                                "for(i = 0; i < buttonArray.length; i++){ " +
                                " if(buttonArray[i].innerText != 'VIEW PAYMENTS') { ids[i] = buttonArray[i].id = 'buttonD' + i; }" +
                                "} " +
                                "for(i = 0; i < ids.length; i++){ var elem = document.getElementById(ids[i]); elem.remove(); }" +
                                "document.getElementsByTagName('form')[1].innerHTML = '';" +
                                "})()");


                if(!isPageLoad){
                    if(!errorFound) {
                        RelativeLayout LayoutLoading = (RelativeLayout) findViewById(R.id.LayoutLoading);
                        LayoutLoading.setVisibility(View.GONE);
                    }
                    else
                        {
                            TextView textViewError = (TextView) findViewById(R.id.textViewError);
                            textViewError.setVisibility(View.VISIBLE);
                            ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
                            progressBarLoading.setVisibility(View.INVISIBLE);
                        }
                }
                else
                {
                    isPageLoad = false;
                }





            }




        });



        RelativeLayout LayoutLoading = (RelativeLayout) findViewById(R.id.LayoutLoading);
        LayoutLoading.setVisibility(View.VISIBLE);
        myWebView.loadUrl("https://pool.arqma.com/#/dashboard");



    }

    protected void iniciarNotifications(Context context)
    {


        Intent resultIntent = new Intent(context, CheckDataReceiver.class);
        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                resultIntent,
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Toast.makeText(context, "pool.ArQma.com server check already running", Toast.LENGTH_LONG).show();
        }
        else
        {
            SharedPreferences prefs = getSharedPreferences("SupportAeonAppKeys", MODE_PRIVATE);

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

            Toast.makeText(context, "pool.ArQmA.com server check started every " +  txt, Toast.LENGTH_LONG).show();
        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            try {
                Intent k = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(k);
            } catch(Exception e) {
            }
        }

        if (id == R.id.action_about) {
            try {
                Uri uri = Uri.parse("https://pool.arqma.com/#/androidapp"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch(Exception e) {
            }
        }

        if (id == R.id.action_website) {
            try {
                Uri uri = Uri.parse("https://pool.arqma.com"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch(Exception e) {
            }
        }


        return super.onOptionsItemSelected(item);
    }
}
