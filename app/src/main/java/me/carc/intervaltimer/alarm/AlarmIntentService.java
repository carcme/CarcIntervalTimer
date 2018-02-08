package me.carc.intervaltimer.alarm;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

import me.carc.intervaltimer.App;
import me.carc.intervaltimer.sound.SoundServices;

/**
 * Created by bamptonm on 7/3/17.
 */

public class AlarmIntentService extends IntentService {

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    private static final int NOTIFY_ID = 1333;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFY_ID, new Notification());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (Build.VERSION.SDK_INT >= 26) {
            SoundServices ss = ((App)getApplicationContext()).getSoundServices();
            AlarmReceiver.playSound(getApplicationContext(), ss);
        }
//        if (Build.VERSION.SDK_INT >= 26) AlarmReceiver.sendNotification(getApplicationContext(), intent);
    }
}
