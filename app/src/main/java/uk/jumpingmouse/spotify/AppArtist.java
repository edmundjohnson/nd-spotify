package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * An object of this class represents an artist.
 * @author Edmund Johnson
 */
public class AppArtist implements Serializable {
    private final String id;
    private final String name;
    private final Bitmap image;

    public AppArtist(String id, String name, Bitmap image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }
}
