package me.carc.intervaltimer.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

/**
 * Notification service for Android.O
 * Created by bamptonm on 24/01/2018.
 */

@TargetApi(Build.VERSION_CODES.O)
public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "me.carc.intervaltimer.ALARM";
    public static final String ANDROID_CHANNEL_NAME = "Workout Channel";

    public NotificationUtils(Context context) {
        super(context);
        createChannels();
    }

    public void createChannels() {
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID, ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        androidChannel.enableLights(false);
        androidChannel.enableVibration(false);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(androidChannel);
    }

    public NotificationChannel getChannel() {
        return getManager().getNotificationChannel(ANDROID_CHANNEL_ID);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}