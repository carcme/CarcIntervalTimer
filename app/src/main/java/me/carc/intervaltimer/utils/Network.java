package me.carc.intervaltimer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Carc.me on 18.04.16.
 * <p/>
 * Get the status of the network connectivity
 */
public class Network {

    public static final int TYPE_NC     = 0;
    public static final int TYPE_WIFI   = 1;
    public static final int TYPE_MOBILE = 2;

    private static int status = TYPE_NC;

    public static boolean connected() {
        return status == ConnectivityManager.TYPE_WIFI || status == ConnectivityManager.TYPE_MOBILE;
    }

    private static int getStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (null != activeNetwork) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                    return TYPE_WIFI;

                if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                    return TYPE_MOBILE;
            }
        }

        return TYPE_NC;
    }

    public static boolean connected(Context context) {
        return Network.getStatus(context) != TYPE_NC;

    }
}