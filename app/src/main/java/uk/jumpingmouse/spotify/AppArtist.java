package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;

/**
 * An object of this class represents an artist.
 * @author Edmund Johnson
 */
public class AppArtist {
    private String name;
    private Bitmap image;

    public AppArtist(String name, Bitmap image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }
}
