package com.dieam.reactnativepushnotification.dst;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DSTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DSTManager dstManager = new DSTManager();
        //dstManager.dstStatus(intent.getExtras().getString("status"));
        dstManager.scheduleDSTTransitionAlarms(context);
        /* Will be removed just for debugging purposes*/
        Long today = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MM yyyy hh:mm:a", Locale.getDefault());
        Log.e("Notification ", "re-scheduled on " + simpleDateFormat.format(today) + " to 9:00 AM");
    }
}