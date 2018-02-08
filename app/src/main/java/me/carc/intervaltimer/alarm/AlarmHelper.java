package me.carc.intervaltimer.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import me.carc.intervaltimer.ui.MainActivity;

import static android.content.Context.ALARM_SERVICE;

public class AlarmHelper {

    private static final String TAG = AlarmHelper.class.getName();

    private static final int INTENT_ID = 124;
    public static final String EXTRA_ALARM_STATE = "EXTRA_ALARM_STATE";

    private Context mContext;
    private AlarmManager alarmManager;


    public void init(Context context) {
        this.mContext = context;
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    private Intent getAlarmIntent(MainActivity.State state) {
        Intent intent = new Intent(mContext.getApplicationContext(), AlarmReceiver.class);
        intent.putExtra(EXTRA_ALARM_STATE, state.name());
        return intent;
    }

    private PendingIntent getPendingIntent(MainActivity.State state) {
        return PendingIntent.getBroadcast(mContext.getApplicationContext(), INTENT_ID, getAlarmIntent(state), PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Add alarm to AlarmManager
     * @param time future time for alarm
     */
    public void setAlarm(long time, MainActivity.State state) {
        setAlarm(mContext.getApplicationContext(), time, getPendingIntent(state));
    }

    /**
     * Remove  alarm from AlarmManager
     */
    public void removeAlarm(MainActivity.State state) {
        final PendingIntent pendingIntent = getPendingIntent(state);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    private void setAlarm(Context context, long time, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }
}
