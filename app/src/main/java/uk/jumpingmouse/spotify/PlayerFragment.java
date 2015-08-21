package uk.jumpingmouse.spotify;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * The fragment containing the track list.
 */
public class PlayerFragment extends DialogFragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String ARG_TRACK = "TRACK";

    private static final String KEY_TRACK = "TRACK_ID";

    private static final String MINUTES_SECONDS_FORMAT = "%d:%d";

    /** The track which is to be played. */
    private AppTrack appTrack;

    /**
     * Instantiates and returns a new PlayerFragment for a supplied track.
     * @param appTrack the track
     * @return a PlayerFragment for the track
     */
    public static PlayerFragment newInstance(AppTrack appTrack) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(PlayerFragment.ARG_TRACK, appTrack);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns this fragment's track.
     * @return this fragment's track
     */
    private AppTrack getTrack() {
        Bundle arguments = getArguments();
        return (arguments == null) ? null : (AppTrack) arguments.getParcelable(ARG_TRACK);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        //this.setHasOptionsMenu(true);
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                    final Bundle savedInstanceState) {
        appTrack = getTrack();

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.player, container, false);

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        TextView txtTrack = (TextView) rootView.findViewById(R.id.txtTrack);
        TextView txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);
        TextView txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        ImageView imgAlbum = (ImageView) rootView.findViewById(R.id.imgAlbum);
        TextView txtTimeEnd = (TextView) rootView.findViewById(R.id.txtTimeEnd);

        if (appTrack != null) {
            txtTrack.setText(appTrack.getTrackName());
            txtArtist.setText(appTrack.getArtistName());
            txtAlbum.setText(appTrack.getAlbumName());
            Picasso.with(getActivity()).load(appTrack.getImageUrlLarge()).into(imgAlbum);
            txtTimeEnd.setText(getHumanReadableMilliseconds(appTrack.getPreviewDuration()));
        }

        return rootView;
    }

    private String getHumanReadableMilliseconds(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(minutes);

        return String.format(MINUTES_SECONDS_FORMAT, minutes, seconds);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     * It does not get called when using the DialogWhenLarge theme for the activity.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_TRACK, appTrack);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    /**
     * Restore the state of the fragment from a Bundle.
     * @param savedInstanceState the Bundle containing the saved state
     */
    private void restoreState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // restore the track
            appTrack = savedInstanceState.getParcelable(KEY_TRACK);
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
    }

    /**
     * Handle the selection of a menu item.
     * The action bar will automatically handle clicks on the Home/Up button, so long
     * as a parent activity is specified in AndroidManifest.xml.
     * @param item the menu item selected
     * @return whether the event has been consumed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            fetchTracks(appArtist.getId());
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

}
