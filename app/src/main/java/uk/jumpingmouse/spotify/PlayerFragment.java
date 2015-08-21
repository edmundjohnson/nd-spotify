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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * The fragment containing the track list.
 */
public class PlayerFragment extends DialogFragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String ARG_TRACKS = "TRACK_ARRAY";
    public static final String ARG_POSITION = "POSITION";

    private static final String KEY_TRACKS = "KEY_TRACK_ARRAY";
    private static final String KEY_POSITION = "KEY_POSITION";

    private static final String MINUTES_SECONDS_FORMAT = "%d:%d";

    /** The list of tracks available to the player. */
    private AppTrack[] appTrackArray;
    /** The position in the list of the currently playing track. */
    private int position;

    // Display elements
    private TextView txtTrack;
    private TextView txtArtist;
    private TextView txtAlbum;
    private ImageView imgAlbum;
    private TextView txtTimeEnd;

    public static PlayerFragment newInstance(List<AppTrack> appTrackList, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArray(PlayerFragment.ARG_TRACKS, appTrackList.toArray(new AppTrack[appTrackList.size()]));
        args.putInt(PlayerFragment.ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns an array of this fragment's tracks.
     * @return an array of this fragment's tracks
     */
    private AppTrack[] getTrackArray() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        return (AppTrack[]) arguments.getParcelableArray(ARG_TRACKS);
    }

    /**
     * Returns the position in the list of the initially selected track.
     * @return the position in the list of the initially selected track
     */
    private int getInitialPosition() {
        Bundle arguments = getArguments();
        return (arguments == null) ? -1 : arguments.getInt(ARG_POSITION);
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
        appTrackArray = getTrackArray();
        position = getInitialPosition();

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.player, container, false);

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        txtTrack = (TextView) rootView.findViewById(R.id.txtTrack);
        txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);
        txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        imgAlbum = (ImageView) rootView.findViewById(R.id.imgAlbum);
        txtTimeEnd = (TextView) rootView.findViewById(R.id.txtTimeEnd);
        Button btnPlayPause = (Button)  rootView.findViewById(R.id.btnPlayPause);
        Button btnPrev = (Button)  rootView.findViewById(R.id.btnPrev);
        Button btnNext = (Button)  rootView.findViewById(R.id.btnNext);

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevTrack();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTrack();
            }
        });

        displayTrack(position);

        return rootView;
    }

    private void playPause() {
        Toast.makeText(getActivity(), "Play", Toast.LENGTH_SHORT).show();
    }

    private void prevTrack() {
        if (position > 0) {
            position--;
        }
        displayTrack(position);
    }

    private void nextTrack() {
        if (position < appTrackArray.length - 1) {
            position++;
        }
        displayTrack(position);
    }

    private void displayTrack(int position) {
        if (position >= 0 && position < appTrackArray.length) {
            AppTrack appTrack = appTrackArray[position];
            if (appTrack != null) {
                txtTrack.setText(appTrack.getTrackName());
                txtArtist.setText(appTrack.getArtistName());
                txtAlbum.setText(appTrack.getAlbumName());
                Picasso.with(getActivity()).load(appTrack.getImageUrlLarge()).into(imgAlbum);
                txtTimeEnd.setText(getHumanReadableMilliseconds(appTrack.getPreviewDuration()));
            }
        }
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
        outState.putParcelableArray(KEY_TRACKS, appTrackArray);
        outState.putInt(KEY_POSITION, position);

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
            // restore the track list and position
            appTrackArray = (AppTrack[]) savedInstanceState.getParcelableArray(KEY_TRACKS);
            // restore the track list and position
            position = savedInstanceState.getInt(KEY_POSITION);
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
