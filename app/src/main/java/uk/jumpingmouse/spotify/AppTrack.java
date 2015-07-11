package uk.jumpingmouse.spotify;

import android.graphics.Bitmap;

/**
 * An object of this class represents a track.
 * @author Edmund Johnson
 */
public class AppTrack {
    private final String id;
    private final String trackName;
    private final String albumName;
    private final Bitmap image;

    public AppTrack(String id, String trackName, String albumName, Bitmap image) {
        this.id = id;
        this.trackName = trackName;
        this.albumName = albumName;
        this.image = image;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Bitmap getImage() {
        return image;
    }
}
