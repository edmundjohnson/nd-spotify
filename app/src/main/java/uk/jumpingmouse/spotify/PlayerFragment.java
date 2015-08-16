package uk.jumpingmouse.spotify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * The fragment containing the track list.
 */
public class PlayerFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final String KEY_TRACK = "TRACK_ID";

    /** The track which is to be played. */
    private AppTrack appTrack;

    private TextView txtTrack;
    private TextView txtArtist;
    private TextView txtAlbum;
    private ImageView imgAlbum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);

        appTrack = (AppTrack) getActivity().getIntent().getExtras().get("TRACK");
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                    final Bundle savedInstanceState) {

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.player, container, false);

        // Get a reference to the ListView
        //ListView listviewTrack = (ListView) rootView.findViewById(R.id.listview_track);

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        txtTrack = (TextView) rootView.findViewById(R.id.txtTrack);
        txtArtist = (TextView) rootView.findViewById(R.id.txtArtist);
        txtAlbum = (TextView) rootView.findViewById(R.id.txtAlbum);
        imgAlbum = (ImageView) rootView.findViewById(R.id.imgAlbum);

        txtTrack.setText(appTrack.getTrackName());
        txtArtist.setText(appTrack.getArtistName());
        txtAlbum.setText(appTrack.getAlbumName());
        Picasso.with(getActivity()).load(appTrack.getImageUrlLarge()).into(imgAlbum);


        return rootView;
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
