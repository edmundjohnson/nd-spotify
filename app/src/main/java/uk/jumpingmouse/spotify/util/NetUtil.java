package uk.jumpingmouse.spotify.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class containing internet-related utility methods.
 * @author Edmund Johnson
 */
public class NetUtil {

    /** Private constructor to prevent instantiation. */
    private NetUtil() {
    }

    /**
     * Returns whether the device is connected to the internet.
     * @return true if the device is connected to the internet, false otherwise
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
