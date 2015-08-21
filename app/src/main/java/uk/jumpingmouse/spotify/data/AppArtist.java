package uk.jumpingmouse.spotify.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A parcelable artist.
 * @author Edmund Johnson
 */
public class AppArtist implements Parcelable {

    private final String id;
    private final String name;
    private final String imageUrlSmall;

    /**
     * Public constructor which initialises the artist.
     * @param id the Spotify id for the artist
     * @param name the name of the artist
     * @param imageUrlSmall the URL of a small image of the artist
     */
    public AppArtist(String id, String name, String imageUrlSmall) {
        this.id = id;
        this.name = name;
        this.imageUrlSmall = imageUrlSmall;
    }

    /**
     * Constructor which creates the artist from a parcel.
     * @param parcel the parcel
     */
    private AppArtist(Parcel parcel) {
        this.id = parcel.readString();
        this.name = parcel.readString();
        this.imageUrlSmall = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getId());
        parcel.writeString(getName());
        parcel.writeString(getImageUrlSmall());
    }

    public static final Parcelable.Creator<AppArtist> CREATOR
            = new Parcelable.Creator<AppArtist>() {
        public AppArtist createFromParcel(Parcel in) {
            return new AppArtist(in);
        }

        public AppArtist[] newArray(int size) {
            return new AppArtist[size];
        }
    };

    // Getters and setters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrlSmall() {
        return imageUrlSmall;
    }

}
