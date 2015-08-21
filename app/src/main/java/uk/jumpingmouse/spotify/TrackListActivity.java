package uk.jumpingmouse.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import uk.jumpingmouse.spotify.data.AppArtist;
import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * Activity for displaying the list of top tracks for an artist.
 */
public class TrackListActivity extends AppCompatActivity implements TrackListFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);

        AppArtist appArtist = (AppArtist) getIntent().getExtras().get("ARTIST");

        if (savedInstanceState == null) {
            // set the action bar subtitle to the artist's name
            if (getSupportActionBar() != null && appArtist != null) {
                getSupportActionBar().setSubtitle(appArtist.getName());
            }

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            TrackListFragment fragment = TrackListFragment.newInstance(appArtist);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_list_container, fragment)
                    .commit();
        }
    }

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
        // Make the "up" arrow behave like the back button,
        // to ensure the parent's state is restored
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * List fragment callback for when a track has been selected from the track list.
     * @param appTrackList the list of all top tracks for the artist whose track was selected
     * @param position the position in appTrackList of the selected track
     */
    @Override
    public void onTrackSelected(List<AppTrack> appTrackList, int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("TRACK_ARRAY", appTrackList.toArray(new AppTrack[appTrackList.size()]));
        intent.putExtra("POSITION", position);
        startActivity(intent);
    }

}
