package com.dieam.reactnativepushnotification.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.facebook.common.logging.FLog;

import me.leolin.shortcutbadger.Badger;
import me.leolin.shortcutbadger.ShortcutBadger;
import me.leolin.shortcutbadger.impl.SamsungHomeBadger;

/**
 * Helper for setting application launcher icon badge counts.
 * <p>
 * This is a wrapper around {@link ShortcutBadger}, with a couple enhancements:
 * <p>
 * - If the first attempt fails, don't retry. This keeps logs clean, as failed attempts are noisy.
 * - Test and apply a separate method for older Samsung devices, which ShortcutBadger has
 * (perhaps over-aggressively) deprecated. ref: https://github.com/leolin310148/ShortcutBadger/issues/40
 */
public class ApplicationBadgeHelper {

    public static final ApplicationBadgeHelper INSTANCE = new ApplicationBadgeHelper();

    private static final String LOG_TAG = "ApplicationBadgeHelper";
    private static final Badger LEGACY_SAMSUNG_BADGER = new SamsungHomeBadger();

    private Boolean applyAutomaticBadger;
    private Boolean applySamsungBadger;
    private ComponentName componentName;

    private ApplicationBadgeHelper() {
    }

    public void setApplicationIconBadgeNumber(Context context, int number) {
        if (null == componentName) {
            componentName = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName()).getComponent();
        }
        tryAutomaticBadge(context, number);
        tryLegacySamsungBadge(context, number);
    }

    private void tryAutomaticBadge(Context context, int number) {
        if (null == applyAutomaticBadger) {
            applyAutomaticBadger = ShortcutBadger.applyCount(context, number);
            if (applyAutomaticBadger) {
                FLog.i(LOG_TAG, "First attempt to use automatic badger succeeded; permanently enabling method.");
            } else {
                FLog.i(LOG_TAG, "First attempt to use automatic badger failed; permanently disabling method.");
            }
            return;
        } else if (!applyAutomaticBadger) {
            return;
        }
        ShortcutBadger.applyCount(context, number);
    }

    private void tryLegacySamsungBadge(Context context, int number) {
        // First attempt to apply legacy samsung badge. Check if eligible, then attempt it.
        if (null == applySamsungBadger) {
            applySamsungBadger = isLegacySamsungLauncher(context) && applyLegacySamsungBadge(context, number);
            if (applySamsungBadger) {
                FLog.i(LOG_TAG, "First attempt to use legacy Samsung badger succeeded; permanently enabling method.");
            } else {
                FLog.w(LOG_TAG, "First attempt to use legacy Samsung badger failed; permanently disabling method.");
            }
            return;
        } else if (!applySamsungBadger) {
            return;
        }
        applyLegacySamsungBadge(context, number);
    }

    private boolean isLegacySamsungLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo == null || resolveInfo.activityInfo.name.toLowerCase().contains("resolver")) {
            return false;
        }

        String currentHomePackage = resolveInfo.activityInfo.packageName;
        return LEGACY_SAMSUNG_BADGER.getSupportLaunchers().contains(currentHomePackage);
    }

    private boolean applyLegacySamsungBadge(Context context, int number) {
        try {
            LEGACY_SAMSUNG_BADGER.executeBadge(context, componentName, number);
            return true;
        } catch (Exception e) {
            FLog.w(LOG_TAG, "Legacy Samsung badger failed", e);
            return false;
        }
    }
}
