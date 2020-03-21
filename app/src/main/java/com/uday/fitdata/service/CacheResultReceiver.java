package com.uday.fitdata.service;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by Chris Black
 */
@SuppressLint("ParcelCreator")
public class CacheResultReceiver extends ResultReceiver {

    public final static String TAG = "CacheResultReceiver";
    private WeakReference<Receiver> mReceiver;

    public CacheResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = new WeakReference<>(receiver);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            Receiver receiver = mReceiver.get();
            if (receiver != null) {
                receiver.onReceiveResult(resultCode, resultData);
            } else {
                Log.d(TAG, "Weak listener is NULL: " + resultData.getString("ResultTag"));
            }
        }
    }
}
