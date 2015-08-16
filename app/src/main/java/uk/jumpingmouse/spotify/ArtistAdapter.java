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
 * An adapter for the list items in the artist list.
 * @author Edmund Johnson
 */
public class ArtistAdapter extends ArrayAdapter<AppArtist> {

    private final Activity context;
    private final List<AppArtist> artistList;

    /**
     * Constructor which does not require a resource.
     * @param context the current context, used to inflate the layout file
     * @param artistList the list of artists to be displayed
     */
    public ArtistAdapter(Activity context, List<AppArtist> artistList) {
        // The second argument is used when the ArrayAdapter is populating a single TextView.
        // This adapter does not use the second argument, so it is set to 0.
        super(context, 0, artistList);
        this.context = context;
        this.artistList = artistList;
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
        // Get the artist object from the list of artists
        AppArtist artist = getItem(position);

        // If the recycled view is null, inflate the list item layout and assign it
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
        }

        // Populate the image view with the artist image
        ImageView imgArtist = (ImageView) itemView.findViewById(R.id.imgArtist);
        Picasso.with(context).load(artist.getImageUrlSmall()).into(imgArtist);
        // Populate the text view with the artist name
        TextView txtArtist = (TextView) itemView.findViewById(R.id.txtArtist);
        txtArtist.setText(artist.getName());

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
        // Display the top tracks for the selected artist in the track list activity,
        // passing in the artist info
        Intent intent = new Intent(context, TrackListActivity.class);
        intent.putExtra("ARTIST", artistList.get(position));
        context.startActivity(intent);
    }

}
