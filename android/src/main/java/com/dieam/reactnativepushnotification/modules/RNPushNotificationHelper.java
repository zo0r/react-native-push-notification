package com.dieam.reactnativepushnotification.modules;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class RNPushNotificationHelper {
    public static final String FILE_URI_SCHEME = "file://";

    private Application mApplication;
    private Context mContext;

    public RNPushNotificationHelper(Application application, Context context) {
        mApplication = application;
        mContext = context;
    }

    public Class getMainActivityClass() {
      String packageName = mContext.getPackageName();
      Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
      String className = launchIntent.getComponent().getClassName();
      try {
          return Class.forName(className);
      } catch (ClassNotFoundException e) {
          e.printStackTrace();
          return null;
      }
    }

    public void sendNotification(Bundle bundle) {
        Class intentClass = getMainActivityClass();
        if (intentClass == null) {
            return;
        }

        if (bundle.getString("message") == null) {
            return;
        }

        Resources res = mApplication.getResources();
        String packageName = mApplication.getPackageName();

        String title = bundle.getString("title");
        if (title == null) {
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            title = mContext.getPackageManager().getApplicationLabel(appInfo).toString();
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setTicker(bundle.getString("ticker"))
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notification.setContentText(bundle.getString("message"));

        Bitmap largeIconBitmap = getLargeIconBitmap(bundle);

        if (largeIconBitmap != null) {
            notification.setLargeIcon(largeIconBitmap);
        }

        int smallIconResId = getSmallIconResId(bundle);
        notification.setSmallIcon(smallIconResId);

        int notificationID;
        String notificationIDString = bundle.getString("id");

        if ( notificationIDString != null ) {
            notificationID = Integer.parseInt(notificationIDString);
        } else {
            notificationID = (int) System.currentTimeMillis();
        }

        Intent intent = new Intent(mContext, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification", bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notification.setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notification.setContentIntent(pendingIntent);

        Notification info = notification.build();
        info.defaults |= Notification.DEFAULT_VIBRATE;
        info.defaults |= Notification.DEFAULT_SOUND;
        info.defaults |= Notification.DEFAULT_LIGHTS;

        notificationManager.notify(notificationID, info);
    }

    @Nullable
    private Bitmap getLargeIconBitmap(Bundle bundle) {

        String largeIcon = bundle.getString("largeIcon");

        if(largeIcon != null && largeIcon.startsWith(FILE_URI_SCHEME)){
            return  BitmapFactory.decodeFile(largeIcon.substring(FILE_URI_SCHEME.length()));
        } else if (largeIcon != null) {
            Resources res = mApplication.getResources();
            String packageName = mApplication.getPackageName();

            int largeIconResId = res.getIdentifier(largeIcon, "mipmap", packageName);
            return BitmapFactory.decodeResource(res, largeIconResId);
        }
        return null;
    }

    private int getSmallIconResId(Bundle bundle) {
        Resources res = mApplication.getResources();
        String packageName = mApplication.getPackageName();

        int smallIconResId = 0;

        String smallIcon = bundle.getString("smallIcon");

        if ( smallIcon != null ) {
            smallIconResId = res.getIdentifier(smallIcon, "mipmap", packageName);
        }
        if ( smallIconResId == 0 ) {
            smallIconResId = android.R.drawable.ic_dialog_info;
        }
        return smallIconResId;
    }

    public void cancelAll() {
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();
    }
}
