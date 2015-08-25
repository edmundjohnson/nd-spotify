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

    private EditText mEditArtistName;

    private ArrayList<AppArtist> mArtistList;
    private ArtistAdapter mArtistAdapter;

    private View mRootView;

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
        mArtistList = new ArrayList<>();
        mArtistAdapter = new ArtistAdapter(getActivity(), mArtistList);

        // Inflate the fragment
        mRootView = inflater.inflate(R.layout.artist_list, container, false);

        // Get a reference to the artist name edit text box
        mEditArtistName = (EditText) mRootView.findViewById(R.id.editArtistName);

        mEditArtistName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Editable editable = ((EditText) view).getText();
                    if (editable != null) {
                        artistNameEntered(editable.toString());
                    }
                    return true;
                }
                return false;
            }
        });

        // Get a reference to the ListView
        ListView listviewArtist = (ListView) mRootView.findViewById(R.id.listview_artist);
        // Attach the adapter to the ListView
        listviewArtist.setAdapter(mArtistAdapter);

        // Create a listener for clicking on the list item.
        listviewArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // Call the item click handler in the activity in which the list is being displayed
                AppArtist artist = mArtistAdapter.getArtistList().get(position);
                ArtistListFragment.Callback callbackActivity = (ArtistListFragment.Callback) getActivity();
                callbackActivity.onArtistSelected(artist);
            }
        });

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mEditArtistName != null && mEditArtistName.getText() != null) {
            outState.putString(KEY_SEARCH_STRING, mEditArtistName.getText().toString());
        } else {
            outState.putString(KEY_SEARCH_STRING, "");
        }
        outState.putParcelableArrayList(KEY_ARTIST_LIST, mArtistList);

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
            mEditArtistName.setText(searchString);
            // restore the artist list
            List<AppArtist> updatedAppArtistList = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            mArtistList.clear();
            if (updatedAppArtistList != null) {
                for (AppArtist appArtist : updatedAppArtistList) {
                    mArtistList.add(appArtist);
                }
            }
            mArtistAdapter.notifyDataSetChanged();
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
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            if (mEditArtistName != null && mEditArtistName.getText() != null) {
//                artistNameEntered(mEditArtistName.getText().toString());
//            }
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform actions on entry of an artist name, i.e. inform the calling activity and
     * invoke an async task to refresh the list of artists.
     * @param artistName the string against which to match artist names.
     */
    private void artistNameEntered(final String artistName) {
        if (artistName != null && !artistName.isEmpty()) {
            if (NetUtil.isConnected(getActivity())) {
                // Deactivate any list item view which is activated
                ListView artistListView = (ListView) mRootView.findViewById(R.id.listview_artist);
                int checkedItem = artistListView.getCheckedItemPosition();
                artistListView.setItemChecked(checkedItem, false);
                // Tell the calling activity that a new artist has been selected
                // e.g. so it can clear any displayed tracks
                ArtistListFragment.Callback callbackActivity = (ArtistListFragment.Callback) getActivity();
                callbackActivity.onArtistNameEntered();
                // Get the artists which match the entered name
                FetchArtistsTask fetchArtistsTask = new FetchArtistsTask(new ArtistListFragmentCallback() {
                    @Override
                    public void displayNoArtistsMessage() {
                        if (isAdded()) {
                            String message = String.format(getString(R.string.no_matching_artists), artistName);
                            UiUtil.displayMessage(getActivity(), message);
                        }
                    }
                });
                fetchArtistsTask.execute(artistName);
            } else {
                UiUtil.displayMessage(getActivity(), getString(R.string.error_not_connected));
            }
        }
    }

    private interface ArtistListFragmentCallback {
        void displayNoArtistsMessage();
    }

    /**
     * Background task for getting the list of matching artists from Spotify.
     */
    public class FetchArtistsTask extends AsyncTask<String, Void, List<AppArtist>> {
        private String searchString = null;

        private final ArtistListFragmentCallback mCallback;

        public FetchArtistsTask(ArtistListFragmentCallback callback) {
            mCallback = callback;
        }

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
                mCallback.displayNoArtistsMessage();
                return;
            }

            // update the adapter's data object
            mArtistList.clear();
            for (AppArtist appArtist : updatedArtistList) {
                mArtistList.add(appArtist);
            }
            // notify the adapter that its data object has changed
            mArtistAdapter.notifyDataSetChanged();
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
        void onArtistNameEntered();

        /**
         * List fragment callback for when an artist has been selected.
         */
        void onArtistSelected(AppArtist artist);
    }

}
