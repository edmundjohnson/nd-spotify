package uk.jumpingmouse.spotify;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;

import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;


/**
 * The fragment containing the artist list.
 */
public class ArtistListFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    private List<Artist> artistList;
    //private List<Artist> adapterArtistList;
    private List<String> adapterArtistList;

    private ArrayAdapter<String> artistAdapter;

    /**
     * Default constructor.
     */
    public ArtistListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_artist_list, menu);
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
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchArtistsTask().execute("Pink");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                    final Bundle savedInstanceState) {

        adapterArtistList = new ArrayList<>();

        artistAdapter = new ArrayAdapter<>(
                // the current context
                getActivity(),
                // the list item layout
                R.layout.artist_list_item,
                // the view to populate
                R.id.txtArtist,
                // the list data
                adapterArtistList);
/*
        List<Artist> adapterArtistList = new ArrayList<>();

        ArrayAdapter<Artist> artistAdapter = new ArrayAdapter<>(
                // the current context
                getActivity(),
                // the list item layout
                R.layout.artist_list_item,
                // the view to populate
                R.id.txtArtist,
                // the list data
                adapterArtistList);
*/
        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.artist_list, container, false);
        // Get a reference to the ListView
        ListView listviewArtist = (ListView) rootView.findViewById(R.id.listview_artist);
        // Attach the adapter to the ListView
        listviewArtist.setAdapter(artistAdapter);

        return rootView;
    }

    /**
     * Background task for getting a weather forecast from OpenWeatherMap.
     */
    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        @Override
        protected List<Artist> doInBackground(String[] params) {

            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchArtistsTask requires a single parameter, the artist name");
            }

            SpotifyApi api = new SpotifyApi();

            // Most (but not all) of the Spotify Web API endpoints require authorisation.
            // If you know you'll only use the ones that don't require authorisation you can skip this step
            //api.setAccessToken("myAccessToken");

            SpotifyService spotify = api.getService();

            ArtistsPager artistsPager = spotify.searchArtists(params[0]);
            if (artistsPager != null) {
                Pager<Artist> artistPager = artistsPager.artists;
                if (artistPager != null) {
                    artistList = artistPager.items;
                    for (Artist artist : artistList) {
                        Log.d(LOG_TAG, "Artist name: " + artist.name);
                    }
                }
            }

            return artistList;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         * @param artistList The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<Artist> artistList) {
            if (artistList == null) {
                return;
            }

            // Update the adapter's data object and notify the adapter
            adapterArtistList.clear();
            for (Artist artist : artistList) {
                adapterArtistList.add(artist.name);
            }
            artistAdapter.notifyDataSetChanged();

            // the superclass method is currently empty
            //super.onPostExecute(forecastData);
        }

    }
}
