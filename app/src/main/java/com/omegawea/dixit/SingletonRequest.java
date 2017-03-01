package com.omegawea.dixit;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

/**
 * Created by SF on 2/20/2017.
 */

public class SingletonRequest {
    public static final String TAG = "MyTag";
    private static SingletonRequest mInstance;
    private RequestQueue requestQueue;
    //private ImageLoader mImageLoader;
    private static Context mCtx;
    Response.Listener<String> errorlistener;

    private SingletonRequest(Context context) {
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized SingletonRequest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SingletonRequest(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void cancelQueue() {
        requestQueue.cancelAll(TAG);
    }

//    public ImageLoader getImageLoader() {
//        return mImageLoader;
//    }
}