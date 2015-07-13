package uk.jumpingmouse.spotify;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Class containing utility methods related to the user interface.
 * @author Edmund Johnson
 */
public class UiUtil {

    /** Private constructor to prevent instantiation. */
    private UiUtil() {
    }

    /**
     * Displays a message as a Toast.
     * @param message the message to display
     */
    public static void displayMessage(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
