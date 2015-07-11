package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
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
    private static final String QUERY_COUNTRY_CODE = "GB";

    /** The id of the artist whose top tracks are to be listed. */
    private String artistId;
    /** The name of the artist whose top tracks are to be listed. */
    private String artistName;

    /** The list of top tracks for the artist. */
    private List<AppTrack> appTrackList;

    /** The adapter for the track list. */
    private ArrayAdapter<AppTrack> trackAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);

        artistId = getActivity().getIntent().getExtras().getString("ARTIST_ID");
        artistName = getActivity().getIntent().getExtras().getString("ARTIST_NAME");
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
            fetchTracks(artistId);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        // Fetch the top tracks for the artist in another thread
        fetchTracks(artistId);

        return rootView;
    }

    /**
     * Perform an async task to refresh the list of tracks.
     * @param artistId the id of the artist whose top tracks are to be displayed.
     */
    private void fetchTracks(String artistId) {
        if (artistId != null && !artistId.isEmpty()) {
            new FetchTracksTask().execute(artistId);
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
            List<AppTrack> appTrackList = new ArrayList<>();

            // Check we have the artist id as a parameter
            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchTracksTask requires a single parameter, the artist id");
            }
            artistId = params[0];

            // Get the Spotify top tracks for the artist and create local tracks from them
            List<Track> trackList = getSpotifyArtistTopTracks(artistId, QUERY_COUNTRY_CODE);
            if (trackList != null) {
                for (Track track : trackList) {
                    appTrackList.add(spotifyTrackToAppTrack(track));
                }
            }

            return appTrackList;
        }

        /**
         * Runs on the UI thread after {@link #doInBackground}.
         * This method won't be invoked if the task was cancelled.
         * @param updatedAppTrackList the track list, as returned by {@link #doInBackground}.
         */
        @Override
        protected void onPostExecute(List<AppTrack> updatedAppTrackList) {
            if (updatedAppTrackList == null || updatedAppTrackList.size() == 0) {
                String message = String.format(getString(R.string.no_matching_tracks_for_artist), artistName);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                return;
            }

            // update the adapter's data object
            appTrackList.clear();
            for (AppTrack appTrack : updatedAppTrackList) {
                appTrackList.add(appTrack);
            }
            // notify the adapter that its data object has changed
            trackAdapter.notifyDataSetChanged();
        }

        /**
         * Returns a list of the Spotify top tracks for an artist in a country.
         * @param artistId the artist id
         * @param countryCode the country code
         * @return a list of the Spotify top tracks for the artist in the country
         */
        private List<Track> getSpotifyArtistTopTracks(String artistId, String countryCode) {
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
         * Creates and returns a local track corresponding to a supplied Spotify track.
         * @param track the Spotify track
         * @return a local track corresponding to the supplied Spotify track
         */
        private AppTrack spotifyTrackToAppTrack(Track track) {
            // Fetch the image for the track's album and store it as a bitmap
            Bitmap imageBitmap = getImageBitmapForAlbum(track.album);
            return new AppTrack(track.id, track.name, getAlbumName(track), imageBitmap);
        }

        /**
         * Returns a bitmap image for an album.
         * @param album the spotify album
         * @return a bitmap image for the album
         */
        private Bitmap getImageBitmapForAlbum(AlbumSimple album) {
            if (album != null
                    && album.images != null
                    && album.images.size() > 0
                    && album.images.get(0).url != null
                    && !album.images.get(0).url.trim().isEmpty()) {
                return NetUtil.getBitmapFromURL(album.images.get(0).url);
            }
            return null;
        }

        /**
         * Returns the album name for a spotify track.
         * @param track the spotify track
         * @return the album name for the spotify track, or "Unknown" if this could not
         *         be determined
         */
        private String getAlbumName(Track track) {
            if (track != null
                    && track.album != null
                    && track.album.name != null
                    && !track.album.name.trim().isEmpty()) {
                return track.album.name;
            } else {
                return getString(R.string.unknown_album_name);
            }
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
