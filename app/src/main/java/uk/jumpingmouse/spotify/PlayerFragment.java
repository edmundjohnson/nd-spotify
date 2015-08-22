package uk.jumpingmouse.spotify;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * The fragment containing the track player.
 */
public class PlayerFragment extends DialogFragment {
    /**
     * The log tag for this class.
     */
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    public static final String ARG_TRACKS = "TRACK_ARRAY";
    public static final String ARG_POSITION = "POSITION";

    private static final String KEY_TRACKS = "KEY_TRACK_ARRAY";
    private static final String KEY_POSITION = "KEY_POSITION";

    private static final String MINUTES_SECONDS_FORMAT = "%d:%d";

    /**
     * The list of tracks available to the player.
     */
    private List<AppTrack> mTrackList;
    /**
     * The position in the list of the currently selected track.
     */
    private int mPosition;

    /**
     * The media player object.
     */
    private MediaPlayer mMediaPlayer;

    /**
     * The media player states, see:
     * http://developer.android.com/reference/android/media/MediaPlayer.html.
     */
    private enum PlayerState {
        IDLE,
        INITIALISED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ERROR,
        END
    }

    ;

    /**
     * The current state of the media player.
     */
    private PlayerState mPlayerState;

    // Display elements
    private TextView txtTrack;
    private TextView txtArtist;
    private TextView txtAlbum;
    private ImageView imgAlbum;
    private TextView txtTimeEnd;
    private Button btnPlayPause;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        //this.setHasOptionsMenu(true);
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        mTrackList = getTrackList();
        mPosition = getInitialPosition();

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
        btnPlayPause = (Button) rootView.findViewById(R.id.btnPlayPause);
        Button btnPrev = (Button) rootView.findViewById(R.id.btnPrev);
        Button btnNext = (Button) rootView.findViewById(R.id.btnNext);

        //--------------------------------------------------------
        // Callbacks for buttons

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

        // Change to the current track position - this will create a new media player
        // and start playing the track
        changePosition(mPosition);

        return rootView;
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //--------------------------------------------------------
        // Callbacks for media player events

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                changeStatePrepared();
                changeStateStarted();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                changeStatePlaybackCompleted();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                Toast.makeText(getActivity(), "A media error has occurred", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, String.format("MediaPlayer error: What: %d. Extra: %d", what, extra));
                changeStateEnd();
                return false;
            }
        });

        return mediaPlayer;
    }

    @Override
    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            changeStatePaused();
        }
        super.onPause();
    }

    /** Change state to IDLE. */
    private void changeStateIdle() {
        if (mMediaPlayer == null) {
            mMediaPlayer = createMediaPlayer();
            mPlayerState = PlayerState.IDLE;
        }
    }

    /** Change state to INITIALISED. */
    private void changeStateInitialised() {
        if (mMediaPlayer != null &&
                mPlayerState == PlayerState.IDLE) {
            try {
                mMediaPlayer.setDataSource(getActivity(), getPreviewUri());
            } catch (Exception e) {
                Log.e(LOG_TAG, String.format("Exception while setting media player data source to %s: %s",
                        getPreviewUri(), e));
                changeStateEnd();
                return;
            }
            mPlayerState = PlayerState.INITIALISED;
        }
    }

    /** Change state to PREPARING. */
    private void changeStatePreparing() {
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.INITIALISED
                || mPlayerState == PlayerState.STOPPED) {
            mMediaPlayer.prepareAsync();
            mPlayerState = PlayerState.PREPARING;
        }
    }

    /** Change state to PREPARED. */
    private void changeStatePrepared() {
        mPlayerState = PlayerState.PREPARED;
    }

    /** Change state to STARTED. */
    private void changeStateStarted() {
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.PREPARED
                || mPlayerState == PlayerState.PAUSED
                || mPlayerState == PlayerState.PLAYBACK_COMPLETED)
        displayPlayPauseButtonAsPause();
        mMediaPlayer.start();
        mPlayerState = PlayerState.STARTED;
    }

    /** Change state to PAUSED. */
    private void changeStatePaused() {
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.STARTED) {
            displayPlayPauseButtonAsPlay();
            mMediaPlayer.pause();
            mPlayerState = PlayerState.PAUSED;
        }
    }

    /** Change state to STOPPED. */
    private void changeStateStopped() {
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.PREPARED
                || mPlayerState == PlayerState.STARTED
                || mPlayerState == PlayerState.PAUSED
                || mPlayerState == PlayerState.PLAYBACK_COMPLETED) {
            mMediaPlayer.stop();
            displayPlayPauseButtonAsPlay();
            mPlayerState = PlayerState.STOPPED;
        }
    }

    /** Change state to PLAYBACK_COMPLETED. */
    private void changeStatePlaybackCompleted() {
        displayPlayPauseButtonAsPlay();
        mPlayerState = PlayerState.PLAYBACK_COMPLETED;
    }

    /** Change state to END. */
    private void changeStateEnd() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mPlayerState = PlayerState.END;
    }

    /** Change the PlayPause button to the pause icon. */
    private void displayPlayPauseButtonAsPause() {
        setButtonIcon(btnPlayPause, android.R.drawable.ic_media_pause);
    }

    /** Change the PlayPause button to the play icon. */
    private void displayPlayPauseButtonAsPlay() {
        setButtonIcon(btnPlayPause, android.R.drawable.ic_media_play);
    }

    /**
     * Set the background icon for a button
     * @param button the button
     * @param resId the icon's drawable resource id
     */
    private void setButtonIcon(Button button, int resId) {
        button.setBackground(getResources().getDrawable(resId));
    }

    /**
     * Toggle the play/pause status of the current track.
     */
    private void playPause() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                changeStatePaused();
            } else {
                changeStateStarted();
            }
        }
    }

    /**
     * Move to the previous track.
     */
    private void prevTrack() {
        if (mPosition > 0) {
            mPosition--;
            changePosition(mPosition);
        }
    }

    /**
     * Move to the next track.
     */
    private void nextTrack() {
        if (mPosition < mTrackList.size() - 1) {
            mPosition++;
            changePosition(mPosition);
        }
    }

    /**
     * Change the position of the current track in the list.
     * @param newPosition the new position of the track in the list
     */
    private void changePosition(int newPosition) {
        if (newPosition >= 0 && newPosition < mTrackList.size()) {
            // End the previously selected track and free up resources
            changeStateEnd();

            // Change the current position
            mPosition = newPosition;

            // Display the details of the new track
            AppTrack appTrack = getTrack();
            if (appTrack != null) {
                txtTrack.setText(appTrack.getTrackName());
                txtArtist.setText(appTrack.getArtistName());
                txtAlbum.setText(appTrack.getAlbumName());
                Picasso.with(getActivity()).load(appTrack.getImageUrlLarge()).into(imgAlbum);
                txtTimeEnd.setText(getHumanReadableMilliseconds(appTrack.getPreviewDuration()));
            }

            // Create a new instance of the media player
            changeStateIdle();
            // Initialise the media player by loading the current track
            changeStateInitialised();
            // Start playing the current track
            changeStatePreparing();
        }
    }

    /**
     * Convert a time in long format to a human-readable string.
     * @param millis a time as a long
     * @return the time in human-readable format
     */
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
    @NonNull
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
        outState.putParcelableArrayList(KEY_TRACKS, (ArrayList<AppTrack>) mTrackList);
        outState.putInt(KEY_POSITION, mPosition);

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
            mTrackList = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
            mPosition = savedInstanceState.getInt(KEY_POSITION);
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

    /**
     * Create and return a new instance of the fragment.
     * @param trackList the list of tracks to which the fragment has access
     * @param position  the position of the selected track in the list
     * @return a new instance of the fragment
     */
    public static PlayerFragment newInstance(List<AppTrack> trackList, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.ARG_TRACKS, (ArrayList<AppTrack>) trackList);
        args.putInt(PlayerFragment.ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a list of this fragment's tracks.
     *
     * @return a list of this fragment's tracks
     */
    private List<AppTrack> getTrackList() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        return arguments.getParcelableArrayList(ARG_TRACKS);
    }

    /**
     * Returns the position in the list of the initially selected track.
     *
     * @return the position in the list of the initially selected track
     */
    private int getInitialPosition() {
        Bundle arguments = getArguments();
        return (arguments == null) ? -1 : arguments.getInt(ARG_POSITION);
    }

    /**
     * Returns the current track.
     * @return the current track
     */
    private AppTrack getTrack() {
        if (mTrackList == null || mPosition == -1) {
            return null;
        }
        return mTrackList.get(mPosition);
    }

    private Uri getPreviewUri() {
        AppTrack track = getTrack();
        if (track == null || track.getPreviewUrl() == null) {
            return null;
        }
        return Uri.parse(track.getPreviewUrl());

    }
}
