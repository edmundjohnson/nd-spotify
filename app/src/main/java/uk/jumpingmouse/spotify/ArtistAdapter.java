package uk.jumpingmouse.spotify;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * An adapter for the list items in the artist list.
 * @author Edmund Johnson
 */
public class ArtistAdapter extends ArrayAdapter<AppArtist> {

    /**
     * Constructor which does not require a resource.
     * @param context the current context, used to inflate the layout file
     * @param artists the list of artist objects to be displayed
     */
    public ArtistAdapter(Activity context, List<AppArtist> artists) {
        // The second argument is used when the ArrayAdapter is populating a single TextView.
        // This adapter does not use the second argument, so it is set to 0.
        super(context, 0, artists);
    }

    /**
     * Get the view for a list item at a specified position.
     * @param position the position in the list
     * @param convertView the recycled view
     * @param parent the parent ViewGroup that is used for inflation
     * @return the View for the list item at the specified position
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the artist object from the list of artists
        AppArtist appArtist = getItem(position);

        // If the recycled view is null, inflate the list item layout and assign it
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);
        }

        // Find the views in the list item layout and populate them with the artist info
        ImageView imgArtist = (ImageView) convertView.findViewById(R.id.imgArtist);
        imgArtist.setImageBitmap(appArtist.getImage());
        TextView txtArtist = (TextView) convertView.findViewById(R.id.txtArtist);
        txtArtist.setText(appArtist.getName());

        return convertView;
    }

}
