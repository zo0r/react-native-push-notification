package main.java.com.dieam.reactnativepushnotification.modules;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Intent;
import android.os.Bundle;


public class RNPushNotificationQueue {

    private static RNPushNotificationQueue instance;

    private Queue<Bundle> queue;

    private boolean loaded;

    private Bundle tempIntent;

    private RNPushNotificationQueue()
    {
        queue = new ArrayBlockingQueue<Bundle>(100);
    }

    public static RNPushNotificationQueue getInstance() {
        if (instance == null)
        {
            instance = new RNPushNotificationQueue();
        }

        return instance;
    }

    public void setInstance(RNPushNotificationQueue instance) {
        this.instance = instance;
    }

    public void push(Bundle bundle) {
        tempIntent = bundle;
        queue.add(bundle);
    }

    public Bundle pop() {
        return tempIntent;
        //return queue.remove();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
