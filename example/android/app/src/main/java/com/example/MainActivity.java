package com.example;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.PowerManager;
import android.view.WindowManager;

import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is
   * used to schedule rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "example";
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true);
      setTurnScreenOn(true);
    } else {
      PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
      PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myapp:wakeLock");
      wl.acquire();

      getWindow().addFlags(
              WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
              | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
              | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
              | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
      );
    }
  }
}
