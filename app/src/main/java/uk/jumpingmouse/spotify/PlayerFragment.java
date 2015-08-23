package uk.jumpingmouse.spotify;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
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
import android.widget.SeekBar;
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
public class PlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
    /**
     * The log tag for this class.
     */
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final String ARG_TRACKS = "TRACK_LIST";
    private static final String ARG_POSITION = "TRACK_POSITION";

    private static final String KEY_TRACKS = "KEY_TRACK_LIST";
    private static final String KEY_TRACK_POSITION = "KEY_TRACK_POSITION";
    private static final String KEY_PLAYER_STATE = "KEY_PLAYER_STATE";
    private static final String KEY_SONG_POSITION = "KEY_SONG_POSITION";

    private static final String MINUTES_SECONDS_FORMAT = "%d:%02d";

    private static final String WIFI_LOCK_TAG = "WIFI_LOCK_TAG";

    private static final int SEEKBAR_INCREMENTS = 300;

    /**
     * The list of tracks available to the player.
     */
    private List<AppTrack> mTrackList;
    /**
     * The position in the list of the currently selected track.
     */
    private int mTrackPosition = -1;

    /**
     * The media player object.
     */
    private MediaPlayer mMediaPlayer;

    /** The wifi lock object. */
    private WifiManager.WifiLock mWifiLock;

    private int mSongPosition = 0;
    private boolean mStartWhenPrepared;


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
        PAUSED_AWAITING_RESTART,
        STOPPED,
        PLAYBACK_COMPLETED,
//        ERROR,
        END
    }

    /**
     * The current state of the media player.
     */
    private PlayerState mPlayerState;

    // Display elements
    private TextView txtTrack;
    private TextView txtArtist;
    private TextView txtAlbum;
    private ImageView imgAlbum;
    private TextView txtTimePosition;
    private TextView txtTimeEnd;
    private SeekBar sbProgress;
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

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.player, container, false);

        txtTrack = (TextView) rootView.findViewById(R.id.txtTrack);
        txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);
        txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        imgAlbum = (ImageView) rootView.findViewById(R.id.imgAlbum);
        txtTimePosition = (TextView) rootView.findViewById(R.id.txtTimePosition);
        txtTimeEnd = (TextView) rootView.findViewById(R.id.txtTimeEnd);
        btnPlayPause = (Button) rootView.findViewById(R.id.btnPlayPause);
        sbProgress = (SeekBar) rootView.findViewById(R.id.sbProgress);
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

        sbProgress.setOnSeekBarChangeListener(this);

        // If there is no saved state, initialise the state.
        if (savedInstanceState == null) {
            mTrackList = getTrackList();

            sbProgress.setMax(SEEKBAR_INCREMENTS);
            displayProgressIndicators(0);

            // Change to the track position selected by the user -
            // this will create a new media player and start playing the track
            changeTrackPosition(getInitialTrackPosition());
        }

        return rootView;
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getActivity(), PowerManager.PARTIAL_WAKE_LOCK);

        //--------------------------------------------------------
        // Callbacks for media player events

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                changeStatePrepared();
                if (mStartWhenPrepared) {
                    changeStateStarted();
                }
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
    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            changeStatePausedAwaitingRestart();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean startWhenPrepared = (mPlayerState == PlayerState.PAUSED_AWAITING_RESTART);

        // Create a new instance of the media player
        changeStateIdle();
        // Initialise the media player by loading the current track
        changeStateInitialised();
        // Prepare the current track and start it if it was playing
        changeStatePreparing(startWhenPrepared);

        if (mPlayerState == PlayerState.PAUSED_AWAITING_RESTART) {
            changeStateStarted();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_TRACKS, (ArrayList<AppTrack>) mTrackList);
        outState.putInt(KEY_TRACK_POSITION, mTrackPosition);
        outState.putString(KEY_PLAYER_STATE, mPlayerState.name());
        outState.putInt(KEY_SONG_POSITION, mSongPosition);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        restoreState(savedInstanceState);
    }

    /**
     * Restore the state of the fragment from a Bundle.
     * @param savedInstanceState the Bundle containing the saved state
     */
    private void restoreState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // restore the track list and position
            mTrackList = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
            mTrackPosition = savedInstanceState.getInt(KEY_TRACK_POSITION);
            mPlayerState = PlayerState.valueOf(savedInstanceState.getString(KEY_PLAYER_STATE));
            mSongPosition = savedInstanceState.getInt(KEY_SONG_POSITION);

            sbProgress.setMax(SEEKBAR_INCREMENTS);
            displayTrackDetails(getTrack());
            displayProgressIndicators(mSongPosition);
        }
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
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.IDLE) {
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
    private void changeStatePreparing(boolean startWhenPrepared) {
        if (mMediaPlayer != null
                && (mPlayerState == PlayerState.INITIALISED
                || mPlayerState == PlayerState.STOPPED)) {
            acquireWifiLock();
            mStartWhenPrepared = startWhenPrepared;
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
                && (mPlayerState == PlayerState.PREPARED
                || mPlayerState == PlayerState.PAUSED
                || mPlayerState == PlayerState.PAUSED_AWAITING_RESTART
                || mPlayerState == PlayerState.PLAYBACK_COMPLETED)) {

            displayPlayPauseButtonAsPause();
            acquireWifiLock();
            mMediaPlayer.seekTo(mSongPosition);
            mMediaPlayer.start();
            mPlayerState = PlayerState.STARTED;
        }

        new MonitorProgressTask().execute();
    }

    /** Change state to PAUSED. */
    private void changeStatePaused() {
        if (mMediaPlayer != null
                && mPlayerState == PlayerState.STARTED) {
            displayPlayPauseButtonAsPlay();
            mMediaPlayer.pause();
            mSongPosition = mMediaPlayer.getCurrentPosition();
            releaseWifiLock();
            mPlayerState = PlayerState.PAUSED;
        }
    }

    /** Change state to PAUSED_AWAITING_RESTART. */
    private void changeStatePausedAwaitingRestart() {
        changeStatePaused();
        mPlayerState = PlayerState.PAUSED_AWAITING_RESTART;
    }

//    /** Change state to STOPPED. */
//    private void changeStateStopped() {
//        if (mMediaPlayer != null
//                && (mPlayerState == PlayerState.PREPARED
//                || mPlayerState == PlayerState.STARTED
//                || mPlayerState == PlayerState.PAUSED
//                || mPlayerState == PlayerState.PLAYBACK_COMPLETED)) {
//            mMediaPlayer.stop();
//            releaseWifiLock();
//            displayPlayPauseButtonAsPlay();
//            mPlayerState = PlayerState.STOPPED;
//        }
//    }

    /** Change state to PLAYBACK_COMPLETED. */
    private void changeStatePlaybackCompleted() {
        mSongPosition = 0;
        if (getActivity() != null) {
            displayPlayPauseButtonAsPlay();
            // The last progress check may have been before the end of the song
            displayProgressIndicators(mSongPosition);
        }
        releaseWifiLock();
        mPlayerState = PlayerState.PLAYBACK_COMPLETED;
    }

    /** Change state to END. */
    private void changeStateEnd() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        releaseWifiLock();
        mPlayerState = PlayerState.END;
    }

    /**
     * Update the seekbar and time position text to indicate a supplied time
     * position in the song.
     * @param songPositionMillis the time into the song in milliseconds
     */
    private void displayProgressIndicators(int songPositionMillis) {
        sbProgress.setProgress(songPositionMillis / 100);
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(songPositionMillis);
        txtTimePosition.setText(getHumanReadableSeconds(seconds));
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
        if (mTrackPosition > 0) {
            mTrackPosition--;
            changeTrackPosition(mTrackPosition);
        }
    }

    /**
     * Move to the next track.
     */
    private void nextTrack() {
        if (mTrackPosition < mTrackList.size() - 1) {
            mTrackPosition++;
            changeTrackPosition(mTrackPosition);
        }
    }

    /**
     * Change the position of the current track in the list.
     * @param newPosition the new position of the track in the list
     */
    private void changeTrackPosition(int newPosition) {
        if (newPosition >= 0 && newPosition < mTrackList.size() && newPosition != mTrackPosition) {
            // End the previously selected track and free up resources
            changeStateEnd();

            // Change the current position
            mTrackPosition = newPosition;

            // Display the details of the new track
            displayTrackDetails(getTrack());

            // Set the progress indicators to the start of the track
            displayProgressIndicators(0);

            // Create a new instance of the media player
            changeStateIdle();
            // Initialise the media player by loading the current track
            changeStateInitialised();
            // Start playing the current track
            changeStatePreparing(true);
        }
    }

    private void displayTrackDetails(AppTrack appTrack) {
        if (appTrack != null) {
            txtTrack.setText(appTrack.getTrackName());
            txtArtist.setText(appTrack.getArtistName());
            txtAlbum.setText(appTrack.getAlbumName());
            Picasso.with(getActivity()).load(appTrack.getImageUrlLarge()).into(imgAlbum);
            txtTimeEnd.setText(getHumanReadableMilliseconds(appTrack.getPreviewDuration()));
        }
    }

    /**
     * Convert a time interval as a number of milliseconds into a human-readable string.
     * @param millis the time interval in milliseconds
     * @return the time interval in human-readable format
     */
    private String getHumanReadableMilliseconds(long millis) {
        return getHumanReadableSeconds(TimeUnit.MILLISECONDS.toSeconds(millis));
    }

    /**
     * Convert a time interval as a number of seconds into a human-readable string.
     * @param totalSeconds the time interval in seconds
     * @return the time interval in human-readable format
     */
    private String getHumanReadableSeconds(long totalSeconds) {
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds);
        long seconds = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(MINUTES_SECONDS_FORMAT, minutes, seconds);
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
     * @return the position in the list of the initially selected track
     */
    private int getInitialTrackPosition() {
        Bundle arguments = getArguments();
        return (arguments == null) ? -1 : arguments.getInt(ARG_POSITION);
    }

    /**
     * Returns the current track.
     * @return the current track
     */
    private AppTrack getTrack() {
        if (mTrackList == null || mTrackPosition == -1) {
            return null;
        }
        return mTrackList.get(mTrackPosition);
    }

    private Uri getPreviewUri() {
        AppTrack track = getTrack();
        if (track == null || track.getPreviewUrl() == null) {
            return null;
        }
        return Uri.parse(track.getPreviewUrl());

    }

    //----------------------------------------------------------------
    // Seekbar change methods

    /**
     * Called when the seekbar position moves
     * @param seekBar the seekbar
     * @param progress the position of the seekbar as a number 0..SEEKBAR_INCREMENTS
     * @param fromUser whether the seekbar was moved by the user
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // Determine how far into the song in milliseconds
            int songPositionMillis = (int) TimeUnit.SECONDS.toMillis(progress / 10);
            // display the new song position in the progress indicators
            displayProgressIndicators(songPositionMillis);
            // move the media player to the new song position
            mSongPosition = songPositionMillis;
            mMediaPlayer.seekTo(mSongPosition);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void acquireWifiLock() {
        if (mWifiLock == null) {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
            mWifiLock.acquire();
        }
    }

    private void releaseWifiLock() {
        if (mWifiLock != null) {
            mWifiLock.release();
            mWifiLock = null;
        }
    }

    /**
     * Background task to monitor the progress of the song and update the seekbar and
     * song position indicator accordingly.
     */
    private class MonitorProgressTask extends AsyncTask<Void, Integer, Void> {
        private static final int CHECK_INTERVAL_MS = 90;

        @Override
        protected Void doInBackground(Void... params) {
            while (mMediaPlayer.isPlaying()) {
                publishProgress((int) mMediaPlayer.getCurrentPosition());
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, String.format("InterruptedException in monitor task: %s", e.toString()));
                    break;
                }
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... songPositionMillis) {
            displayProgressIndicators(songPositionMillis[0]);
        }

    }
}
