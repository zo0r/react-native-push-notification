package com.dieam.reactnativepushnotification.dst.models;

public class NotificationData {
    private String id;
    private String message;
    private long fireDate;
    private String title;
    private boolean showWhen;
    private boolean autoCancel;
    private boolean groupSummary;
    private boolean playSound = true;
    private String soundName;
    private boolean vibrate;
    private float vibration;
    private boolean invokeApp;
    private String repeatType;
    private float repeatTime;
    private float when;
    private boolean usesChronometer;
    private float timeoutAfter;
    private boolean onlyAlertOnce;
    private boolean ongoing;
    private boolean allowWhileIdle;
    private boolean ignoreInForeground;


    // Getter Methods

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getFireDate() {
        return fireDate;
    }

    public String getTitle() {
        return title;
    }

    public boolean getShowWhen() {
        return showWhen;
    }

    public boolean getAutoCancel() {
        return autoCancel;
    }

    public boolean getGroupSummary() {
        return groupSummary;
    }

    public boolean getPlaySound() {
        return playSound;
    }

    public boolean getVibrate() {
        return vibrate;
    }

    public float getVibration() {
        return vibration;
    }

    public boolean getInvokeApp() {
        return invokeApp;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public float getRepeatTime() {
        return repeatTime;
    }

    public float getWhen() {
        return when;
    }

    public boolean getUsesChronometer() {
        return usesChronometer;
    }

    public float getTimeoutAfter() {
        return timeoutAfter;
    }

    public boolean getOnlyAlertOnce() {
        return onlyAlertOnce;
    }

    public boolean getOngoing() {
        return ongoing;
    }

    public boolean getAllowWhileIdle() {
        return allowWhileIdle;
    }

    public boolean getIgnoreInForeground() {
        return ignoreInForeground;
    }

    private String getSoundName() {
        return soundName;
    }
    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFireDate(long fireDate) {
        this.fireDate = fireDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShowWhen(boolean showWhen) {
        this.showWhen = showWhen;
    }

    public void setAutoCancel(boolean autoCancel) {
        this.autoCancel = autoCancel;
    }

    public void setGroupSummary(boolean groupSummary) {
        this.groupSummary = groupSummary;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public void setVibration(float vibration) {
        this.vibration = vibration;
    }

    public void setInvokeApp(boolean invokeApp) {
        this.invokeApp = invokeApp;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public void setRepeatTime(float repeatTime) {
        this.repeatTime = repeatTime;
    }

    public void setWhen(float when) {
        this.when = when;
    }

    public void setUsesChronometer(boolean usesChronometer) {
        this.usesChronometer = usesChronometer;
    }

    public void setTimeoutAfter(float timeoutAfter) {
        this.timeoutAfter = timeoutAfter;
    }

    public void setOnlyAlertOnce(boolean onlyAlertOnce) {
        this.onlyAlertOnce = onlyAlertOnce;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public void setAllowWhileIdle(boolean allowWhileIdle) {
        this.allowWhileIdle = allowWhileIdle;
    }

    public void setIgnoreInForeground(boolean ignoreInForeground) {
        this.ignoreInForeground = ignoreInForeground;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }

}
