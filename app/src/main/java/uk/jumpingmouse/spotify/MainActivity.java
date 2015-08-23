package uk.jumpingmouse.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import uk.jumpingmouse.spotify.data.AppArtist;
import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * The main activity.
 */
public class MainActivity extends AppCompatActivity
        implements ArtistListFragment.Callback, TrackListFragment.Callback {

    private static final String TRACKLISTFRAGMENT_TAG = "TRACK_LIST_FRAGMENT_TAG";

    private boolean mMultiPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.track_list_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mMultiPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.track_list_container, new TrackListFragment(), TRACKLISTFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mMultiPane = false;
            // shadow not wanted in single pane mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0f);
            }
        }
    }

    /* Maybe update the track list here if there are any relevant changes to the settings.
    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ArtistListFragment ff = (ArtistListFragment) getSupportFragmentManager().findFragmentById(R.id.artist_list_fragment);
            if ( null != ff ) {
                ff.onLocationChanged(null);
            }
            TrackListFragment trackListFragment = (TrackListFragment) getSupportFragmentManager().findFragmentByTag(TRACKLISTFRAGMENT_TAG);
            if (null != trackListFragment) {
                trackListFragment.onLocationChanged(location);
            }
            mLocation = location;
        }
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Start the settings activity
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * List fragment callback for when a new artist name has been entered.
     */
    @Override
    public void onArtistNameEntered() {
        if (mMultiPane) {
            // Remove any tracks which are being displayed for the previously
            // entered artist
            TrackListFragment fragment = TrackListFragment.newInstance(null);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_list_container, fragment, TRACKLISTFRAGMENT_TAG)
                    .commit();
        }
    }

    /**
     * List fragment callback for when an artist has been selected from the artist list.
     * @param appArtist the artist which was selected
     */
    @Override
    public void onArtistSelected(AppArtist appArtist) {
        if (mMultiPane) {
            // Show the track list view in this activity by adding or
            // replacing the track list fragment using a fragment transaction.
            TrackListFragment fragment = TrackListFragment.newInstance(appArtist);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_list_container, fragment, TRACKLISTFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TrackListActivity.class);
            intent.putExtra("ARTIST", appArtist);
            startActivity(intent);
        }
    }

    /**
     * List fragment callback for when a track has been selected from the track list.
     * @param appTrackList the list of all top tracks for the artist whose track was selected
     * @param position the position in appTrackList of the selected track
     */
    @Override
    public void onTrackSelected(List<AppTrack> appTrackList, int position) {
//        if (mMultiPane) {
        // In multi-pane mode, show the player view in this activity by displaying
        // the player fragment in a dialog using a fragment transaction.
        PlayerFragment fragment = PlayerFragment.newInstance(appTrackList, position);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.show(getSupportFragmentManager(), "dialog");
        transaction.commit();

//        } else {
        // This never happens - if in single-pane mode, the track list is
        // being displayed in the TrackListActivity
//            Intent intent = new Intent(this, PlayerActivity.class);
//            intent.putExtra("TRACK", appTrack);
//            startActivity(intent);
//        }

    }

}
