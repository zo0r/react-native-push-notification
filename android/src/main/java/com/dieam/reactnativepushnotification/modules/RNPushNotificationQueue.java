package main.java.com.dieam.reactnativepushnotification.modules;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Intent;


public class RNPushNotificationQueue {

    private static RNPushNotificationQueue instance;

    private Queue<Intent> queue;

    private boolean loaded;

    private RNPushNotificationQueue()
    {
        queue = new ArrayBlockingQueue<Intent>(100);
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

    public void push(Intent intent) {
        queue.add(intent);
    }

    public Intent pop() {
        return queue.remove();
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
