package main.java.com.dieam.reactnativepushnotification.modules;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Intent;


public class RNPushNotificationQueue {

    private static RNPushNotificationQueue instance;

    private Queue<Intent> queue;

    private boolean hasLoaded;

    private RNPushNotificationQueue()
    {
        queue = new ArrayBlockingQueue<Intent>();
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

    public boolean isHasLoaded() {
        return hasLoaded;
    }

    public void setHasLoaded(boolean hasLoaded) {
        this.hasLoaded = hasLoaded;
    }
}
