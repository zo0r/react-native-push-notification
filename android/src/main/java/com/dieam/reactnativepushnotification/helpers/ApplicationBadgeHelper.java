package com.dieam.reactnativepushnotification.helpers;

import android.content.Context;

import com.facebook.common.logging.FLog;

import me.leolin.shortcutbadger.ShortcutBadger;

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
    private Boolean applyAutomaticBadger;

    private ApplicationBadgeHelper() {
    }

    public void setApplicationIconBadgeNumber(Context context, int number) {
        tryAutomaticBadge(context, number);
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
}
