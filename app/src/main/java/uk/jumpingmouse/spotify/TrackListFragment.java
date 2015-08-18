package uk.jumpingmouse.spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * The fragment containing the track list.
 */
public class TrackListFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = TrackListFragment.class.getSimpleName();

    private static final String QUERY_COUNTRY_KEY = "country";

    private static final String KEY_ARTIST = "ARTIST_NAME";
    private static final String KEY_TRACK_LIST = "TRACK_LIST";

    private static final long PREVIEW_DURATION_MS = 30000;

    /** The artist whose top tracks are to be listed. */
    private AppArtist appArtist;

    /** The list of top tracks for the artist. */
    private ArrayList<AppTrack> appTrackList;

    /** The adapter for the track list. */
    private ArrayAdapter<AppTrack> trackAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);

        appArtist = (AppArtist) getActivity().getIntent().getExtras().get("ARTIST");
    }

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                    final Bundle savedInstanceState) {

        // Initialise the track list and adapter
        appTrackList = new ArrayList<>();
        trackAdapter = new TrackAdapter(getActivity(), appTrackList);

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.track_list, container, false);

        // Get a reference to the ListView
        ListView listviewTrack = (ListView) rootView.findViewById(R.id.listview_track);
        // Attach the adapter to the ListView
        listviewTrack.setAdapter(trackAdapter);

        // Restore any saved state
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            // Fetch the top tracks for the artist in another thread
            fetchTracks(appArtist.getId());
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_ARTIST, appArtist);
        outState.putParcelableArrayList(KEY_TRACK_LIST, appTrackList);

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
            // restore the artist
            appArtist = savedInstanceState.getParcelable(KEY_ARTIST);
            //set action bar subtitle ?
            // restore the track list
            List<AppTrack> updatedAppTrackList = savedInstanceState.getParcelableArrayList(KEY_TRACK_LIST);
            appTrackList.clear();
            for (AppTrack appTrack : updatedAppTrackList) {
                appTrackList.add(appTrack);
            }
            trackAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_track_list, menu);
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
            fetchTracks(appArtist.getId());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform an async task to refresh the list of tracks.
     * @param artistId the id of the artist whose top tracks are to be displayed.
     */
    private void fetchTracks(String artistId) {
        if (artistId != null && !artistId.isEmpty()) {
            if (NetUtil.isConnected(getActivity())) {
                new FetchTracksTask().execute(artistId);
            } else {
                UiUtil.displayMessage(getActivity(), getString(R.string.error_not_connected));
            }
        }
    }

    /**
     * Background task for getting the list of top tracks for an artist from Spotify.
     */
    public class FetchTracksTask extends AsyncTask<String, Void, List<AppTrack>> {

        /**
         * Background task to fetch a list of the top tracks for an artist from Spotify
         * and return the list.
         * @param params the parameters
         * @return the list of tracks as supplied by Spotify
         */
        @Override
        protected List<AppTrack> doInBackground(String[] params) {
            // Check we have the artist id as a parameter
            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchTracksTask requires a single parameter, the artist id");
            }
            String artistId = params[0];
            String countryCode = getPreference(getActivity(), R.string.pref_country_code_key, R.string.pref_country_code_default);

            if (NetUtil.isConnected(getActivity())) {
                // Get the Spotify top tracks for the artist and return them
                return getArtistTopAppTracks(artistId, countryCode);
            } else {
                return new ArrayList<>();
            }
        }

        /**
         * Returns a current preference.
         * @param context the context
         * @param key the string resource id of the preference's key
         * @param defaultValue the string resource id of the preference's default value
         * @return the current preference setting for the preference
         */
        private String getPreference(Context context, int key, int defaultValue) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(getString(key), getString(defaultValue));
        }

        /**
         * Load the track list created in the background into the adapter.
         * Runs on the UI thread after {@link #doInBackground}.
         * This method won't be invoked if the task was cancelled.
         * @param updatedTrackList the track list, as returned by {@link #doInBackground}.
         */
        @Override
        protected void onPostExecute(List<AppTrack> updatedTrackList) {
            if (updatedTrackList == null || updatedTrackList.size() == 0) {
                String message = String.format(
                        getString(R.string.no_matching_tracks_for_artist), appArtist.getName());
                UiUtil.displayMessage(getActivity(), message);
                return;
            }

            // update the adapter's data object
            appTrackList.clear();
            for (AppTrack track : updatedTrackList) {
                appTrackList.add(track);
            }
            // notify the adapter that its data object has changed
            trackAdapter.notifyDataSetChanged();
        }

        /**
         * Returns a list of the top tracks for an artist in a country.
         * @param artistId the artist id
         * @param countryCode the country code
         * @return a list of the top tracks for the artist in the country
         */
        private List<AppTrack> getArtistTopAppTracks(String artistId, String countryCode) {
            List<AppTrack> appTrackList = new ArrayList<>();
            List<Track> trackList = getArtistTopSpotifyTracks(artistId, countryCode);
            if (trackList != null) {
                for (Track track : trackList) {
                    AppTrack appTrack = new AppTrack(
                            track.id,
                            track.name,
                            SpotifyUtil.getAlbumName(track, getActivity().getString(R.string.unknown_album_name)),
                            SpotifyUtil.getImageUrlSmallForAlbum(track.album),
                            SpotifyUtil.getImageUrlLargeForAlbum(track.album),
                            track.preview_url,
                            track.duration_ms,
                            PREVIEW_DURATION_MS,
                            SpotifyUtil.getArtistName(track, getActivity().getString(R.string.unknown_artist_name))
                            );
                    appTrackList.add(appTrack);
                }
            }
            return appTrackList;
        }

        /**
         * Returns a list of the top Spotify tracks for an artist in a country.
         * This method must be run in a background thread.
         * @param artistId the artist id
         * @param countryCode the country code
         * @return a list of the top Spotify tracks for the artist in the country
         */
        private List<Track> getArtistTopSpotifyTracks(String artistId, String countryCode) {
            try {
                Map<String, Object> options = new HashMap<>();
                options.put(QUERY_COUNTRY_KEY, countryCode);
                Tracks tracks = getSpotifyService().getArtistTopTrack(artistId, options);
                if (tracks != null) {
                    return tracks.tracks;
                }
            } catch (RetrofitError e) {
                Log.e(LOG_TAG, "RetrofitError while fetching track list: " + e);
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
