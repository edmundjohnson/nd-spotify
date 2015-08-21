package uk.jumpingmouse.spotify;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.RetrofitError;
import uk.jumpingmouse.spotify.data.AppArtist;
import uk.jumpingmouse.spotify.util.NetUtil;
import uk.jumpingmouse.spotify.util.SpotifyUtil;
import uk.jumpingmouse.spotify.util.UiUtil;


/**
 * The fragment containing the artist list.
 */
public class ArtistListFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    private static final String KEY_SEARCH_STRING = "SEARCH_STRING";
    private static final String KEY_ARTIST_LIST = "ARTIST_LIST";

    private EditText editArtistName;

    private ArrayList<AppArtist> appArtistList;
    private ArtistAdapter artistAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);
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
                if (keyCode == KeyEvent.KEYCODE_ENTER){
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

        // Create a listener for clicking on the list item.
        listviewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // Call the item click handler in the activity in which the list is being displayed
                AppArtist artist = artistAdapter.getArtistList().get(position);
                ArtistListFragment.Callback callbackActivity = (ArtistListFragment.Callback) getActivity();
                callbackActivity.onArtistSelected(artist);
            }
        });

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (editArtistName != null && editArtistName.getText() != null) {
            outState.putString(KEY_SEARCH_STRING, editArtistName.getText().toString());
        } else {
            outState.putString(KEY_SEARCH_STRING, "");
        }
        outState.putParcelableArrayList(KEY_ARTIST_LIST, appArtistList);

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
            // restore the search string
            String searchString = savedInstanceState.getString(KEY_SEARCH_STRING);
            editArtistName.setText(searchString);
            // restore the artist list
            List<AppArtist> updatedAppArtistList = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            appArtistList.clear();
            if (updatedAppArtistList != null) {
                for (AppArtist appArtist : updatedAppArtistList) {
                    appArtistList.add(appArtist);
                }
            }
            artistAdapter.notifyDataSetChanged();
        }
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

    /**
     * Invoke an async task to refresh the list of artists.
     * @param artistName the string against which to match artist names.
     */
    private void fetchArtists(String artistName) {
        if (artistName != null && !artistName.isEmpty()) {
            if (NetUtil.isConnected(getActivity())) {
                new FetchArtistsTask().execute(artistName);
            } else {
                UiUtil.displayMessage(getActivity(), getString(R.string.error_not_connected));
            }
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
            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchArtistsTask requires a single parameter, the artist name");
            }
            searchString = params[0];

            if (NetUtil.isConnected(getActivity())) {
                // Fetch the list of artists and return it
                return getAppArtists(searchString);
            }
            // Return an empty list if there is no internet connection
            return new ArrayList<>();
        }

        /**
         * Returns a list of the artists whose names match a supplied string.
         * @param strSearch the string to match against
         * @return a list of the artists whose names match the search string
         */
        private List<AppArtist> getAppArtists(String strSearch) {
            List<AppArtist> appArtistList = new ArrayList<>();

            List<Artist> artistList = getSpotifyArtists(strSearch);
            if (artistList != null) {
                for (Artist artist : artistList) {
                    AppArtist appArtist = new AppArtist(artist.id, artist.name,
                            SpotifyUtil.getImageUrlSmall(artist.images));
                    appArtistList.add(appArtist);
                }
            }
            return appArtistList;
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
         * Load the artist list created in the background into the adapter.
         * Runs on the UI thread after {@link #doInBackground}.
         * This method won't be invoked if the task was cancelled.
         * @param updatedArtistList the artist list, as returned by {@link #doInBackground}.
         */
        @Override
        protected void onPostExecute(List<AppArtist> updatedArtistList) {
            if (updatedArtistList == null || updatedArtistList.size() == 0) {
                String message = String.format(getString(R.string.no_matching_artists), searchString);
                UiUtil.displayMessage(getActivity(), message);
                return;
            }

            // update the adapter's data object
            appArtistList.clear();
            for (AppArtist appArtist : updatedArtistList) {
                appArtistList.add(appArtist);
            }
            // notify the adapter that its data object has changed
            artistAdapter.notifyDataSetChanged();
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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * List fragment callback for when an artist has been selected.
         */
        void onArtistSelected(AppArtist artist);
    }

}
