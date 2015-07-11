package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class containing internet-related utility methods.
 * @author Edmund Johnson
 */
public class NetUtil {
    /** The log tag for this class. */
    private static final String LOG_TAG = NetUtil.class.getSimpleName();

    /**
     * Private constructor to prevent instantiation.
     */
    private NetUtil() {
    }

    /**
     * Returns an image at a URL as a bitmap.
     * @param strUrl the URL of the image
     * @return the image as a Bitmap
     */
    public static Bitmap getBitmapFromURL(@NonNull String strUrl) {
        try {
            Log.d(LOG_TAG, "strUrl: " + strUrl);
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }



}
