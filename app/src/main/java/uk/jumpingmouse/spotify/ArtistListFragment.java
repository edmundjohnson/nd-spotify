package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.RetrofitError;


/**
 * The fragment containing the artist list.
 */
public class ArtistListFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    private EditText editArtistName;

    private List<AppArtist> appArtistList;

    private ArrayAdapter<AppArtist> artistAdapter;

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
            if (editArtistName != null && editArtistName.getText() != null) {
                fetchArtists(editArtistName.getText().toString());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                    final Bundle savedInstanceState) {

        // Initialise the artist list and adapter
        appArtistList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(getActivity(), appArtistList);

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.artist_list, container, false);

        // Get a reference to the artist name edit text box
        editArtistName = (EditText) rootView.findViewById(R.id.editArtistName);

        editArtistName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    Editable editable = ((EditText) view).getText();
                    if (editable != null) {
                        fetchArtists(editable.toString());
                    }
                    return true;
                }
                return false;
            }
        });

        // Get a reference to the ListView
        ListView listviewArtist = (ListView) rootView.findViewById(R.id.listview_artist);
        // Attach the adapter to the ListView
        listviewArtist.setAdapter(artistAdapter);

        return rootView;
    }

    /**
     * Perform an async task to refresh the list of artists.
     * @param artistName the string against which to match artist names.
     */
    private void fetchArtists(String artistName) {
        if (artistName != null && !artistName.isEmpty()) {
            new FetchArtistsTask().execute(artistName);
        }
    }

    /**
     * Background task for getting the list of matching artists from Spotify.
     */
    public class FetchArtistsTask extends AsyncTask<String, Void, List<AppArtist>> {
        private String searchString = null;

        /**
         * Background task to fetch a list of artists from Spotify and return it in a custom list.
         * @param params the parameters
         * @return the list of artists as supplied by Spotify
         */
        @Override
        protected List<AppArtist> doInBackground(String[] params) {
            List<AppArtist> appArtistList = new ArrayList<>();

            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchArtistsTask requires a single parameter, the artist name");
            }
            searchString = params[0];

            // Fetch the list of artists from Spotify and convert them into local artists
            List<Artist> artistList = getSpotifyArtists(searchString);
            if (artistList != null) {
                for (Artist artist : artistList) {
                    appArtistList.add(spotifyArtistToAppArtist(artist));
                }
            }
            return appArtistList;
        }

        /**
         * Runs on the UI thread after {@link #doInBackground}.
         * This method won't be invoked if the task was cancelled.
         * @param updatedAppArtistList the artist list, as returned by {@link #doInBackground}.
         */
        @Override
        protected void onPostExecute(List<AppArtist> updatedAppArtistList) {
            if (updatedAppArtistList == null || updatedAppArtistList.size() == 0) {
                String message = String.format(getString(R.string.no_matching_artists), searchString);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                return;
            }

            // update the adapter's data object
            appArtistList.clear();
            for (AppArtist appArtist : updatedAppArtistList) {
                appArtistList.add(appArtist);
            }
            // notify the adapter that its data object has changed
            artistAdapter.notifyDataSetChanged();
        }

        /**
         * Returns a list of the Spotify artists whose names match a supplied string.
         * @param strSearch the string to match against
         * @return a list of the Spotify artists whose names match the search string
         */
        private List<Artist> getSpotifyArtists(String strSearch) {
            try {
                ArtistsPager artistsPager = getSpotifyService().searchArtists(strSearch);
                if (artistsPager != null) {
                    Pager<Artist> artistPager = artistsPager.artists;
                    if (artistPager != null) {
                        return artistPager.items;
                    }
                }
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "RetrofitError while fetching artist list: " + e);
            }
            return null;
        }

        /**
         * Creates and returns a local artist corresponding to a supplied Spotify artist.
         * @param artist the Spotify artist
         * @return a local artist corresponding to the supplied Spotify artist
         */
        private AppArtist spotifyArtistToAppArtist(Artist artist) {
            // Fetch the image for the artist and store it as a bitmap
            Bitmap imageBitmap = getImageBitmapForArtist(artist);
            return new AppArtist(artist.id, artist.name, imageBitmap);
        }

        /**
         * Returns a bitmap image for an artist
         * @param artist the spotify artist
         * @return a bitmap image for the artist
         */
        private Bitmap getImageBitmapForArtist(Artist artist) {
            if (artist != null
                    && artist.images != null
                    && artist.images.size() > 0
                    && artist.images.get(0).url != null
                    && !artist.images.get(0).url.trim().isEmpty()) {
                    return NetUtil.getBitmapFromURL(artist.images.get(0).url);
            }
            return null;
        }

        /**
         * Return a SpotifyService object.
         * @return a SpotifyService object
         */
        private SpotifyService getSpotifyService() {
            SpotifyApi spotifyApi = new SpotifyApi();

            // Most (but not all) of the Spotify Web API endpoints require authorisation.
            // The ones that require authorisation need the following step:
            //spotifyApi.setAccessToken("myAccessToken");

            return spotifyApi.getService();
        }
    }

}
