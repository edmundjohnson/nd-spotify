package uk.jumpingmouse.spotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;

import uk.jumpingmouse.spotify.data.AppTrack;


/**
 * Activity for displaying the track player.
 */
public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        AppTrack[] appTrackArray = (AppTrack[]) getIntent().getExtras().get("TRACK_ARRAY");
        List<AppTrack> appTrackList = Arrays.asList(appTrackArray);
        int position = (int) getIntent().getExtras().get("POSITION");

        if (savedInstanceState == null) {
            displayFragment(appTrackList, position);
        }
    }

    /**
     * Display the fragment
     */
    private void displayFragment(List<AppTrack> appTrackList, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        PlayerFragment fragment = PlayerFragment.newInstance(appTrackList, position);

        // Show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(R.id.player_container, fragment);
        // The Android documentation suggests the next line,
        // but it causes a blank screen when "back" is pressed
        //transaction.addToBackStack(null);
        transaction.commit();
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

}
