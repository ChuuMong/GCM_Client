package com.example.gcmtestapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements Response.Listener<String>, Response.ErrorListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String SENDER_ID = "1002328388710";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleCloudMessaging gcm;
    private TextView regid_text, receiver_text;
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regid_text = (TextView) findViewById(R.id.regid_text);
        receiver_text = (TextView) findViewById(R.id.receiver_text);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId();

            if (regId.isEmpty()) {
                getRemoteRegistrationId();
            }
        }
        else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        regid_text.setText("Device registered, registration ID=" + regId + "\n");

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + ".GCM_TEST"));
    }

    private void getRemoteRegistrationId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regId = gcm.register(SENDER_ID);
                    sendRegistrationIdToBackend();
                    storeRegistrationId();
                }
                catch (IOException ex) {
                    Log.i(TAG, "Error :" + ex.getMessage());
                }
            }
        }).start();
    }

    private void sendRegistrationIdToBackend() {
        StringRequest request = new StringRequest(Request.Method.POST, "http://fifth-being-92309.appspot.com/register", this, this) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("regId", regId);
                return params;
            }
        };

        HttpStack.getInstance(this).addToRequestQueue(request);
    }

    private void storeRegistrationId() {
        final SharedPreferences prefs = getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);
        prefs.edit().putString("REG_ID", regId).commit();
    }

    private String getRegistrationId() {
        String registrationId = getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE).getString("REG_ID", "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        return registrationId;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + ".GCM_TEST")) {
                if (intent.getStringExtra("msg") != null) {
                    receiver_text.setText(intent.getStringExtra("msg"));
                }
            }
        }
    };

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        regid_text.setText("");
        regid_text.setText("Device registered, registration ID=" + regId + "\n");
    }
}
