package uk.jumpingmouse.spotify;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * An adapter for the list items in a track list.
 * @author Edmund Johnson
 */
public class TrackAdapter extends ArrayAdapter<AppTrack> {

    private final Activity context;
    private final List<AppTrack> trackList;


    /**
     * Constructor which does not require a resource.
     * @param context the current context, used to inflate the layout file
     * @param trackList the list of track objects to be displayed
     */
    public TrackAdapter(Activity context, List<AppTrack> trackList) {
        // The second argument is used when the ArrayAdapter is populating a single TextView.
        // This adapter does not use the second argument, so it is set to 0.
        super(context, 0, trackList);
        this.context = context;
        this.trackList = trackList;
    }

    /**
     * Get the view for a list item at a specified position.
     * @param position the position in the list
     * @param itemView the recycled view
     * @param parent the parent ViewGroup that is used for inflation
     * @return the View for the list item at the specified position
     */
    @Override
    public View getView(final int position, View itemView, ViewGroup parent) {
        // Get the track object from the list of tracks
        AppTrack track = getItem(position);

        // If the recycled view is null, inflate the list item layout and assign it
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, parent, false);
        }

        // Populate the image view element with the track image
        ImageView imgAlbum = (ImageView) itemView.findViewById(R.id.imgAlbum);
        Picasso.with(context).load(track.getImageUrlSmall()).into(imgAlbum);

        // Populate the text view elements with the track name and album name
        TextView txtTrack = (TextView) itemView.findViewById(R.id.txtTrack);
        txtTrack.setText(track.getTrackName());
        TextView txtAlbum = (TextView) itemView.findViewById(R.id.txtAlbum);
        txtAlbum.setText(track.getAlbumName());

        // Set the click handler for the item
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                handleItemClick(position);
            }
        });

        return itemView;
    }

    /**
     * Handler method invoked when an item is clicked.
     * @param position the item's position in the list
     */
    private void handleItemClick(final int position) {
        // Display the player activity, passing in the track info for the selected track
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("TRACK", trackList.get(position));
        context.startActivity(intent);
    }

}
