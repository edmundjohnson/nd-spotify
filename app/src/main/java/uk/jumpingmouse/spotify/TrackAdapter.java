package uk.jumpingmouse.spotify;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;


/**
 * An adapter for the list items in a track list.
 * @author Edmund Johnson
 */
public class TrackAdapter extends ArrayAdapter<Track> {

    private final Activity context;

    /**
     * Constructor which does not require a resource.
     * @param context the current context, used to inflate the layout file
     * @param tracks the list of track objects to be displayed
     */
    public TrackAdapter(Activity context, List<Track> tracks) {
        // The second argument is used when the ArrayAdapter is populating a single TextView.
        // This adapter does not use the second argument, so it is set to 0.
        super(context, 0, tracks);
        this.context = context;
    }

    /**
     * Get the view for a list item at a specified position.
     * @param position the position in the list
     * @param convertView the recycled view
     * @param parent the parent ViewGroup that is used for inflation
     * @return the View for the list item at the specified position
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the track object from the list of tracks
        Track track = getItem(position);

        // If the recycled view is null, inflate the list item layout and assign it
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, parent, false);
        }

        // Populate the image view element with the track image
        ImageView imgAlbum = (ImageView) convertView.findViewById(R.id.imgAlbum);
        String imageUrl = getSmallImageUrlForAlbum(track.album);
        Picasso.with(context).load(imageUrl).into(imgAlbum);

        // Populate the text view elements with the track name and album name
        TextView txtTrack = (TextView) convertView.findViewById(R.id.txtTrack);
        txtTrack.setText(track.name);
        TextView txtAlbum = (TextView) convertView.findViewById(R.id.txtAlbum);
        txtAlbum.setText(getAlbumName(track));

        return convertView;
    }

    /**
     * Returns a URL for a small image for a Spotify album.
     * @param album the Spotify album
     * @return a URL for a small image for the Spotify album.
     */
    private String getSmallImageUrlForAlbum(AlbumSimple album) {
        if (album == null) {
            return null;
        }
        return SpotifyUtil.getSmallImageUrl(album.images);
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
            return context.getString(R.string.unknown_album_name);
        }
    }

}
