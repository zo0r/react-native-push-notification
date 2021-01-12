package com.dieam.reactnativepushnotification.dst;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dieam.reactnativepushnotification.dst.models.DSTData;
import com.dieam.reactnativepushnotification.modules.RNPushNotificationHelper;
import com.google.gson.Gson;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DSTManager {

    private SimpleDateFormat simpleDateFormat;
    private AlarmManager alarmMgr;
    private String DST_STATUS = DST.DST_START.name();
    //private boolean timeZoneChangeReceiverIsRegistered = false;


    public void scheduleDSTTransitionAlarms(Context context) {
        simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm a", Locale.getDefault());
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        List<DSTData> dstList = checkTimeZoneIsOnDSTAndGetDSTData(context, TimeZone.getDefault().getID());
        Calendar currentCalendar = Calendar.getInstance();
        /* Receiver should be registered once*/
        /*if (!timeZoneChangeReceiverIsRegistered && context instanceof Activity) {
            registerTimeZoneChangeReceiver(context);
        }*/
        int dst_start_request_code = DST.DST_START.getValue();
        int dst_end_request_code = DST.DST_END.getValue();
        /* we are using loops because if we have countries with changing dst-start/end date,
        ie; iran have 21/22 march as dst start date*/
        for (DSTData dstObject : dstList) {
            int startMonth = dstObject.getStart_month_index();
            int endMonth = dstObject.getEnd_month_index();
            int startTime = dstObject.getStart_time();
            int endTime = dstObject.getEnd_time();
            int year = currentCalendar.get(Calendar.YEAR);

            int startDay;
            int endDay;
            if (dstObject.getStart_week_day() != null & dstObject.getEnd_week_day() != null) {
                String startWeekDay = dstObject.getStart_week_day().toUpperCase();
                String endWeekDay = dstObject.getEnd_week_day().toUpperCase();
                startDay = getDayFromWeekDay(startWeekDay, startMonth, year);// ie; LSAT_SUNDAY 28
                endDay = getDayFromWeekDay(endWeekDay, startMonth, year);// ie; LAST_SUNDAY 28
            } else {
                startDay = dstObject.getStart_day();
                endDay = dstObject.getEnd_day();
            }

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.MONTH, startMonth);
            startCalendar.set(Calendar.DAY_OF_MONTH, startDay);
            startCalendar.set(Calendar.HOUR_OF_DAY, startTime);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(Calendar.MONTH, endMonth);
            endCalendar.set(Calendar.DAY_OF_MONTH, endDay);
            endCalendar.set(Calendar.HOUR_OF_DAY, endTime);
            endCalendar.set(Calendar.MINUTE, 0);
            endCalendar.set(Calendar.SECOND, 0);
            endCalendar.set(Calendar.MILLISECOND, 0);

            if (currentCalendar.before(startCalendar)) {
                scheduleDSTBroadCast(context, dst_start_request_code, startCalendar);
                dst_start_request_code++;// scheduling multiple notifications if countries
                // have changing dst-start time.
                Log.e("DST 1st Date ", simpleDateFormat.format(startCalendar.getTime()));
            } else if (currentCalendar.before(endCalendar)) {
                scheduleDSTBroadCast(context, dst_end_request_code, endCalendar);
                dst_end_request_code++;
                Log.e("DST 2nd Date ", simpleDateFormat.format(endCalendar.getTime()));
            } else {
                scheduleDSTBroadcastForNextYear(context);
            }
            /*if (DST_STATUS.equals(DST.DST_END.name())) {
                Log.e("############# DST ", "ENDED ################");
                resetNotificationDateInLibraryForDstEnd(context);
            } else {
                resetNotificationDateInLibraryForDstStart(context);
            }*/
            resetNotificationDateInLibrary(context);
        }
    }

    private void resetNotificationDateInLibrary(Context context) {
        RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper(context);
        rnPushNotificationHelper.resetNotificationDate();
    }

    /*void resetNotificationDateInLibraryForDstEnd(Context context) {
        *//*Call the appropriate function in react-library or give a callback to react *//*
        RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper(context);
        rnPushNotificationHelper.resetForDstEnd();
    }*/

    /*void resetNotificationDateInLibraryForDstStart(Context context) {
        *//*Call the appropriate function in react-library or give a callback to react *//*
        RNPushNotificationHelper rnPushNotificationHelper = new RNPushNotificationHelper(context);
        rnPushNotificationHelper.resetForDstStart();
    }*/

    /**
     * to schedule the broadcast to the next year (to handle the recurrence of broadcast)
     */
    private void scheduleDSTBroadcastForNextYear(Context context) {
        int dst_start_request_code = DST.DST_START.getValue();
        /* we are using loops because if we have countries with more than one dst-start date,
        ie; iran have 21/22 march as dst start */
        List<DSTData> dstList = checkTimeZoneIsOnDSTAndGetDSTData(context, TimeZone.getDefault().getID());
        for (DSTData dstObject : dstList) {
            Calendar currentCalendar = Calendar.getInstance();
            int startMonth = dstObject.getStart_month_index();
            int startTime = dstObject.getStart_time();
            int year = currentCalendar.get(Calendar.YEAR) + 1;

            int startDay;
            if (dstObject.getStart_week_day() != null & dstObject.getEnd_week_day() != null) {
                String startWeekDay = dstObject.getStart_week_day().toUpperCase();
                startDay = getDayFromWeekDay(startWeekDay, startMonth, year);// ie; LSAT_SUNDAY 28
            } else {
                startDay = dstObject.getStart_day();
            }

            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.MONTH, startMonth);
            startCalendar.set(Calendar.DAY_OF_MONTH, startDay);
            startCalendar.set(Calendar.HOUR_OF_DAY, startTime);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);
            startCalendar.add(Calendar.YEAR, 1);
            scheduleDSTBroadCast(context, dst_start_request_code, startCalendar);
            dst_start_request_code++;
            Log.e("1st Date Next Year ", simpleDateFormat.format(startCalendar.getTime()));
        }
    }


    /**
     * Register time-zone change BroadcastReceiver
     */
/*
    void registerTimeZoneChangeReceiver(Context context) {
        Log.e("TimezoneChange", "Receiver Registered");
        IntentFilter s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        */
    /* broadcast receiver for manual timezone-change *//*

        final BroadcastReceiver m_timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    Log.e("TIMEZONE CHANGED", "########## ACTION_TIMEZONE_CHANGED ###############");
                    //resetting the broadcast if a timezone change occurred.
                    scheduleDSTTransitionAlarms(context);
                }
            }
        };
        context.registerReceiver(m_timeChangedReceiver, s_intentFilter);
        timeZoneChangeReceiverIsRegistered = true;
    }
*/


    /**
     * here we are finding the decimal digit of the day from weekday name ie; if the weekday name is
     * LAST SUNDAY then we call the getLastDay() method by passing SUNDAY (which is extracted from
     * LAST SUNDAY).
     */
    private int getDayFromWeekDay(String weekDay, int month, int year) {
        if (weekDay.contains("LAST")) {
            return getLastDay(DAY.getDayValue(getDayFromText(weekDay)), month, year);
        } else if (weekDay.contains("FIRST")) {
            return getNthDayOfMonth(getDateTimeConstants(getDayFromText(weekDay)), 1, month, year);
        } else if (weekDay.contains("SECOND")) {
            return getNthDayOfMonth(getDateTimeConstants(getDayFromText(weekDay)), 2, month, year);
        } else if (weekDay.contains("THIRD")) {
            getNthDayOfMonth(getDateTimeConstants(getDayFromText(weekDay)), 3, month, year);
        } else if (weekDay.contains("FOURTH")) {
            return getNthDayOfMonth(getDateTimeConstants(getDayFromText(weekDay)), 4, month, year);
        }
        return 0;
    }

    /**
     * here we are finding the time constants for joda-time library.
     * if the weekday is SUNDAY we find the appropriate decimal from DateTimeConstants in joda
     */
    private int getDateTimeConstants(String day) {
        if (day.equalsIgnoreCase("SUNDAY")) {
            return DateTimeConstants.SUNDAY;
        } else if (day.equalsIgnoreCase("SATURDAY")) {
            return DateTimeConstants.SATURDAY;
        } else if (day.equalsIgnoreCase("FRIDAY")) {
            return DateTimeConstants.FRIDAY;
        } else if (day.equalsIgnoreCase("THURSDAY")) {
            return DateTimeConstants.THURSDAY;
        } else if (day.equalsIgnoreCase("WEDNESDAY")) {
            return DateTimeConstants.WEDNESDAY;
        } else if (day.equalsIgnoreCase("TUESDAY")) {
            return DateTimeConstants.TUESDAY;
        } else {
            return DateTimeConstants.MONDAY;
        }
    }

    /**
     * splitting the weekday which is from database
     */
    private String getDayFromText(String weekDay) {
        return weekDay.trim().split(" ")[1].trim();
    }

    /**
     * converting the json array to ArrayList<String>
     */
    private ArrayList<String> getAsList(JSONArray timeZones) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < timeZones.length(); i++) {
            list.add(timeZones.optString(i));
        }
        return list;
    }


    /**
     * Parsing of json file
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "SingleStatementInBlock"})
    public JSONArray getDSTData(Context context) {
        JSONArray jsonObject;
        try {
            InputStream is = context.getAssets().open("world_dst_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            Charset charset = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                charset = StandardCharsets.UTF_8;
            } else {
                charset = Charset.forName("UTF-8");
            }
            jsonObject = new JSONArray(new String(buffer, charset));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public void dstStatus(String status) {
        DST_STATUS = status;
    }

    /**
     * to get the number to be minus from the month end-date to get the appropriate LAST_DAY of
     * the month
     */
    enum DAY {
        LAST_SUNDAY(-1),
        LAST_SATURDAY(0),
        LAST_FRIDAY(1),
        LAST_THURSDAY(2),
        LAST_WEDNESDAY(3),
        LAST_TUESDAY(4),
        LAST_MONDAY(5);

        int value;

        DAY(int value) {
            this.value = value;
        }

        static int getDayValue(String day) {
            if (day.equalsIgnoreCase("SUNDAY")) {
                return DAY.LAST_SUNDAY.value;
            } else if (day.equalsIgnoreCase("SATURDAY")) {
                return DAY.LAST_SATURDAY.value;
            } else if (day.equalsIgnoreCase("FRIDAY")) {
                return DAY.LAST_FRIDAY.value;
            } else if (day.equalsIgnoreCase("THURSDAY")) {
                return DAY.LAST_THURSDAY.value;
            } else if (day.equalsIgnoreCase("WEDNESDAY")) {
                return DAY.LAST_WEDNESDAY.value;
            } else if (day.equalsIgnoreCase("TUESDAY")) {
                return DAY.LAST_TUESDAY.value;
            } else {
                return DAY.LAST_MONDAY.value;
            }
        }
    }

    /**
     * To get the last day of the month
     */
    public int getLastDay(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month + 1, 1);
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK)) - day);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * To get nth day of the month ie; First Sunday
     */
    public int getNthDayOfMonth(int day, final int n, final int month, final int year) {
        final LocalDate firstSunday = new LocalDate(year, month + 1, 1).withDayOfWeek(day);
        if (n > 1) {
            final LocalDate nThSunday = firstSunday.plusWeeks(n - 1);
            final LocalDate lastDayInMonth = firstSunday.dayOfMonth().withMaximumValue();
            if (nThSunday.isAfter(lastDayInMonth)) {
                //Toast.makeText(this, "There is no " + n + "th day in this month!", Toast.LENGTH_SHORT).show();
                Log.e("Error!", "There is no " + n + "th day in this month!");
                return 0;
            }
            return nThSunday.getDayOfMonth();
        }
        return firstSunday.getDayOfMonth();
    }

    /**
     * Registering the DST broadcast to the alarm manager.
     */
    @SuppressWarnings("SameParameterValue")
    private void scheduleDSTBroadCast(Context context, int request_code, Calendar calendar) {
        Intent intent = new Intent(context, DSTReceiver.class);
        if (request_code == DST.DST_END.value) {
            intent.putExtra("status", DST.DST_END.name());
        } else {
            intent.putExtra("status", DST.DST_START.name());
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, request_code, intent, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private List<DSTData> checkTimeZoneIsOnDSTAndGetDSTData(Context context, String currentTimeZone) {
        JSONArray dstData = getDSTData(context);
        List<DSTData> dstObject = new ArrayList<>();
        for (int i = 0; i < dstData.length(); i++) {
            JSONObject mDstObject = dstData.optJSONObject(i);
            if (getAsList(mDstObject.optJSONArray("TimeZones")).contains(currentTimeZone)) {
                dstObject.add(new Gson().fromJson(mDstObject.toString(), DSTData.class));
            }
        }
        return dstObject;
    }


    /**
     * The request codes for pending intent for the DST broadcast
     */
    enum DST {
        DST_START(100),
        DST_END(201);
        int value;

        DST(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }
    }
}
