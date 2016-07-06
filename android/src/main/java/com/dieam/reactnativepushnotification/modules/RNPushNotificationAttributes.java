package com.dieam.reactnativepushnotification.modules;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class RNPushNotificationAttributes {
    private String id;
    private String message;
    private double fireDate;
    private String title;
    private String ticker;
    private boolean autoCancel;
    private String largeIcon;
    private String smallIcon;
    private String bigText;
    private String subText;
    private String number;
    private String sound;
    private String color;

    public RNPushNotificationAttributes() {

    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("message", message);
        bundle.putDouble("fireDate", fireDate);
        bundle.putString("title", title);
        bundle.putString("ticker", ticker);
        bundle.putBoolean("autoCancel", autoCancel);
        bundle.putString("largeIcon", largeIcon);
        bundle.putString("smallIcon", smallIcon);
        bundle.putString("bigText", bigText);
        bundle.putString("subText", subText);
        bundle.putString("number", number);
        bundle.putString("sound", sound);
        bundle.putString("color", color);
        return bundle;
    }

    public void fromBundle(Bundle bundle) {
        id = bundle.getString("id");
        message = bundle.getString("message");
        fireDate = bundle.getDouble("fireDate");
        title = bundle.getString("title");
        ticker = bundle.getString("ticker");
        autoCancel = bundle.getBoolean("autoCancel");
        largeIcon = bundle.getString("largeIcon");
        smallIcon = bundle.getString("smallIcon");
        bigText = bundle.getString("bigText");
        subText = bundle.getString("subText");
        number = bundle.getString("number");
        sound = bundle.getString("sound");
        color = bundle.getString("color");
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("message", message);
            jsonObject.put("fireDate", fireDate);
            jsonObject.put("title", title);
            jsonObject.put("ticker", ticker);
            jsonObject.put("autoCancel", autoCancel);
            jsonObject.put("largeIcon", largeIcon);
            jsonObject.put("smallIcon", smallIcon);
            jsonObject.put("bigText", bigText);
            jsonObject.put("subText", subText);
            jsonObject.put("number", number);
            jsonObject.put("sound", sound);
            jsonObject.put("color", color);
        } catch (JSONException e) {
            Log.e("RNPushNotification", "Exception while converting RNPushNotificationAttributes to " +
                    "JSON. Returning an empty object", e);
            return new JSONObject();
        }
        return jsonObject;
    }

    public void fromJson(JSONObject jsonObject) {
        try {
            id = jsonObject.has("id") ? jsonObject.getString("id") : null;
            message = jsonObject.has("message") ? jsonObject.getString("message") : null;
            fireDate = jsonObject.has("fireDate") ? jsonObject.getDouble("fireDate") : 0.0;
            title = jsonObject.has("title") ? jsonObject.getString("title") : null;
            ticker = jsonObject.has("ticker") ? jsonObject.getString("ticker") : null;
            autoCancel = jsonObject.has("autoCancel") ? jsonObject.getBoolean("autoCancel") : true;
            largeIcon = jsonObject.has("largeIcon") ? jsonObject.getString("largeIcon") : null;
            smallIcon = jsonObject.has("smallIcon") ? jsonObject.getString("smallIcon") : null;
            bigText = jsonObject.has("bigText") ? jsonObject.getString("bigText") : null;
            subText = jsonObject.has("subText") ? jsonObject.getString("subText") : null;
            number = jsonObject.has("number") ? jsonObject.getString("number") : null;
            sound = jsonObject.has("sound") ? jsonObject.getString("sound") : null;
            color = jsonObject.has("color") ? jsonObject.getString("color") : null;
        } catch (JSONException e) {
            Log.e("RNPushNotification", "Exception while initializing RNPushNotificationAttributes from " +
                    "JSON. Some fields may not be set", e);
        }
    }

    public String getId() {
        return id;
    }

    public double getFireDate() {
        return fireDate;
    }

    public String getTitle() {
        return title;
    }

    public String getTicker() {
        return ticker;
    }

    public boolean isAutoCancel() {
        return autoCancel;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public String getBigText() {
        return bigText;
    }

    public String getSubText() {
        return subText;
    }

    public String getNumber() {
        return number;
    }

    public String getSound() {
        return sound;
    }

    public String getColor() {
        return color;
    }

    public String getMessage() {
        return message;
    }
}
