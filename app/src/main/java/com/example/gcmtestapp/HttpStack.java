package com.example.gcmtestapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by JongHunLee on 2015-04-21.
 */
public class HttpStack {

    private static HttpStack instance = null;
    private RequestQueue mRequestQueue;

    private HttpStack(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static HttpStack getInstance(Context context) {
        if (instance == null) {
            synchronized (HttpStack.class) {
                if (instance == null) {
                    instance = new HttpStack(context);
                }
            }
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
