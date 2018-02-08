package me.carc.intervaltimer.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.Random;

import me.carc.intervaltimer.App;
import me.carc.intervaltimer.R;
import me.carc.intervaltimer.sound.SoundServices;
import me.carc.intervaltimer.settings.Preferences;
import me.carc.intervaltimer.ui.MainActivity;
import me.carc.intervaltimer.utils.NotificationUtils;

/**
 * Catch the sms alarm expire
 * Created by bamptonm on 7/4/17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {

        App app = ((App)context.getApplicationContext());
        if(!app.isActive()) {

            // Insert the sms to the sms database
            Intent alarmIntentService = new Intent(context, AlarmIntentService.class);
            if (intent != null)
                alarmIntentService.putExtras(intent.getExtras());
            alarmIntentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            ContextCompat.startForegroundService(context, alarmIntentService);

            if (Build.VERSION.SDK_INT < 26) {
                playSound(context, app.getSoundServices());
//                sendNotification(context, intent);
            }
        }
    }

    public static void playSound(final Context context, final SoundServices ss) {
        final boolean mute = Preferences.useSounds(context);
        final boolean vibrate = Preferences.useVibrate(context);
        ss.playSingleBeepSound(mute, vibrate);
    }



    public void clearNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    public static void sendNotification(Context context, Intent intent) {

        // extract the sms parameters
        String state = intent.getStringExtra(AlarmHelper.EXTRA_ALARM_STATE);

        // Send sms notification
        Intent smsNotication = new Intent(context, MainActivity.class);
        PendingIntent showSMSIntent = PendingIntent.getActivity(context, 0, smsNotication, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder nBuilder = new Notification.Builder(context, NotificationUtils.ANDROID_CHANNEL_ID);

            nBuilder.setAutoCancel(true);
            nBuilder.setSmallIcon(R.mipmap.ic_launcher, 1);
            nBuilder.setContentTitle(state);

            if(state.equals(MainActivity.State.WORK.name())) {
                nBuilder.setColor(ContextCompat.getColor(context, R.color.colorWork));//Color.rgb(255, 87, 34));
                String[] quotes = context.getResources().getStringArray(R.array.workQuotes);
                Random rand = new Random();
                int i = rand.nextInt(quotes.length);
                nBuilder.setContentText(quotes[i]);

            } else {
                nBuilder.setColor(ContextCompat.getColor(context, R.color.colorRest));//Color.rgb(255, 87, 34));
                String[] quotes = context.getResources().getStringArray(R.array.restQuotes);
                Random rand = new Random();
                int i = rand.nextInt(quotes.length);
                nBuilder.setContentText(quotes[i]);
            }

//            nBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", showSMSIntent);
//            nBuilder.addAction(R.drawable.ic_delete, "Delete", showSMSIntent);

            nBuilder.setContentIntent(showSMSIntent);
            NotificationUtils notificationUtils = new NotificationUtils(context);
            notificationUtils.getManager().notify(2001, nBuilder.build());

        }else {
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context, "MY_CHANNEL");

            nBuilder.setDefaults(Notification.DEFAULT_ALL);
            nBuilder.setAutoCancel(true);
            nBuilder.setSmallIcon(R.mipmap.ic_launcher, 1);
            nBuilder.setContentTitle(state);
            nBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

            if(state.equals(MainActivity.State.WORK.name())) {
                nBuilder.setColor(ContextCompat.getColor(context, R.color.colorWork));//Color.rgb(255, 87, 34));
                String[] quotes = context.getResources().getStringArray(R.array.workQuotes);
                Random rand = new Random();
                int i = rand.nextInt(quotes.length);
                nBuilder.setContentText(quotes[i]);

            } else {
                nBuilder.setColor(ContextCompat.getColor(context, R.color.colorRest));//Color.rgb(255, 87, 34));
                String[] quotes = context.getResources().getStringArray(R.array.restQuotes);
                Random rand = new Random();
                int i = rand.nextInt(quotes.length);
                nBuilder.setContentText(quotes[i]);
            }

//            nBuilder.addAction(android.R.drawable.ic_media_pause, "Pause", showSMSIntent);
//            nBuilder.addAction(R.drawable.ic_delete, "Delete", showSMSIntent);
            nBuilder.setContentIntent(showSMSIntent);


            // get custom ringtone
            Uri resourceURI = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.request_alarm);
            nBuilder.setSound(resourceURI);
            nBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
            nm.notify(2001, nBuilder.build());
        }

        AlarmHelper helper = new AlarmHelper();
        helper.init(context);

//        helper.setAlarm();
    }


    /**
     * Create a round bitmap
     * @param bitmap the bitmap
     * @return the round bitmap
     */
    private static Bitmap getCircleBitmap(Bitmap bitmap) {

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();

        return output;
    }
}