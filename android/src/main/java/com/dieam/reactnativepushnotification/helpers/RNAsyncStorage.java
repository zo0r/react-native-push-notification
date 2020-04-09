package com.dieam.reactnativepushnotification.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.facebook.react.modules.storage.ReactDatabaseSupplier;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNAsyncStorage {
    //private static String ALERTS_CONFIG = "@NEW_KEY_ALERTS:key";
    private static String CURRENT_DEVICE = "@CURRENT_DEVICE:key";
    private static String DEFAULT_USER = "@DEFAULT_USER:key";

    public Context context;
    //public ArrayList<JSONObject> addressList;
    public JSONObject data;

    Cursor catalystLocalStorage = null;
    SQLiteDatabase readableDatabase = null;

    public RNAsyncStorage(Context context) {
        this.context = context;
        data = new JSONObject();
        this.getStorage();
    }

    public void getStorage() {
        try {
            readableDatabase = ReactDatabaseSupplier.getInstance(context).getReadableDatabase();
            String TABLE = "catalystLocalStorage";
            String[] FIELDS = {"key", "value"};
            String WHERE = "key=? OR key=?";
            String[] ARGS = {CURRENT_DEVICE, DEFAULT_USER};
            catalystLocalStorage = readableDatabase.query(TABLE, FIELDS, WHERE, ARGS, null, null, null);
            if (catalystLocalStorage.moveToFirst()) {
                do {
                    try {
                        String keys = catalystLocalStorage.getString(catalystLocalStorage.getColumnIndex("key"));
                        String rawValue = catalystLocalStorage.getString(catalystLocalStorage.getColumnIndex("value"));

                        try {
                            data.put(keys, new JSONObject(rawValue));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "error in catalystLocalStorage" + e.getMessage());
                    }
                } while (catalystLocalStorage.moveToNext());
            }
        } finally {
            if (catalystLocalStorage != null) {
                catalystLocalStorage.close();
            }

            if (readableDatabase != null) {
                readableDatabase.close();
            }
        }
    }

    public boolean getUserETA(){
        String userDevice = getUserDevice();
        if(userDevice != null){
            try{
                JSONObject jsonUser = new JSONObject(userDevice);
                JSONObject jsonSettings = jsonUser.getJSONObject("settings");
                boolean ETA = jsonSettings.getBoolean("eta");
                return ETA;
            }catch (Exception e) {
                Log.e(LOG_TAG, "Error get ETA json "+e);
                return false;
            }
        }
        return false;
    }

    public boolean getUserDisplayCriticalAlerts(){
        String userDevice = getUserDevice();
        if(userDevice != null){
            try{
                JSONObject jsonUser = new JSONObject(userDevice);
                JSONObject jsonSettings = jsonUser.getJSONObject("settings");
                boolean critical = jsonSettings.getBoolean("critical");
                return critical;
            }catch (Exception e) {
                Log.e(LOG_TAG, "Error get critical json "+e);
                return false;
            }
        }
        return false;
    }

    private String getUserDevice() {
        try {
            return data.getString(CURRENT_DEVICE);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error get CURRENT DEVICE "+e);
            return "";
        }
    }

}