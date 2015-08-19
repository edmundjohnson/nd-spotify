package uk.jumpingmouse.spotify.util;

import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Class containing Spotify-related utility methods.
 * @author Edmund Johnson
 */
public class SpotifyUtil {

    private static final int SMALL_IMAGE_SIZE_MIN = 180;
    private static final int SMALL_IMAGE_SIZE_MAX = 220;
    private static final int LARGE_IMAGE_SIZE_MIN = 600;
    private static final int LARGE_IMAGE_SIZE_MAX = 1000;


    /** Private constructor to prevent instantiation. */
    private SpotifyUtil() {
    }

    /**
     * Returns the album name for a spotify track.
     * @param track the spotify track
     * @param defaultName the name to return if the album name could not be determined
     * @return the album name for the spotify track, or the default name if this could not
     *         be determined
     */
    public static String getAlbumName(Track track, String defaultName) {
        if (track != null
                && track.album != null
                && track.album.name != null
                && !track.album.name.trim().isEmpty()) {
            return track.album.name;
        } else {
            return defaultName;
        }
    }

    /**
     * Returns the artist name for a spotify track.
     * @param track the spotify track
     * @param defaultName the name to return if the artist name could not be determined
     * @return the artist name for the spotify track, or the default name if this could not
     *         be determined
     */
    public static String getArtistName(Track track, String defaultName) {
        if (track != null
                && track.artists != null
                && !track.artists.isEmpty()
                && track.artists.get(0) != null) {
            return track.artists.get(0).name;
        } else {
            return defaultName;
        }
    }

    /**
     * Returns a URL for a small image for a Spotify album.
     * @param album the Spotify album
     * @return a URL for a small image for the Spotify album.
     */
    public static String getImageUrlSmallForAlbum(AlbumSimple album) {
        if (album == null) {
            return null;
        }
        return SpotifyUtil.getImageUrlSmall(album.images);
    }

    /**
     * Returns a URL for a small image for a Spotify album.
     * @param album the Spotify album
     * @return a URL for a small image for the Spotify album.
     */
    public static String getImageUrlLargeForAlbum(AlbumSimple album) {
        if (album == null) {
            return null;
        }
        return SpotifyUtil.getImageUrlLarge(album.images);
    }

    /**
     * Returns a URL for a small image from a list of image objects.
     * @param imageList the list of images
     * @return The URL for a small image from the list is returned if found,
     *         otherwise the URL for any image is returned.
     *         If no images are found, null is returned.
     */
    public static String getImageUrlSmall(List<Image> imageList) {
        return getImageUrl(imageList, SMALL_IMAGE_SIZE_MIN, SMALL_IMAGE_SIZE_MAX);
    }

    /**
     * Returns a URL for a large image from a list of image objects.
     * @param imageList the list of images
     * @return The URL for a large image from the list is returned if found,
     *         otherwise the URL for any image is returned.
     *         If no images are found, null is returned.
     */
    private static String getImageUrlLarge(List<Image> imageList) {
        return getImageUrl(imageList, LARGE_IMAGE_SIZE_MIN, LARGE_IMAGE_SIZE_MAX);
    }

    /**
     * Returns a URL for an image from a list of image objects.
     * @param imageList the list of images
     * @param sizeMin the minimum pixel size desired for the image height and width
     * @param sizeMax the maximum pixel size desired for the image height and width
     * @return The URL for the first image matching the size desired is returned if found,
     *         otherwise the URL for the first image is returned.
     *         If no images are found, null is returned.
     */
    private static String getImageUrl(List<Image> imageList, int sizeMin, int sizeMax) {
        if (imageList != null && imageList.size() > 0) {
            for (Image image : imageList) {
                if (image.height >= sizeMin && image.height <= sizeMax
                        && image.width >= sizeMin && image.width <= sizeMax) {
                    return image.url;
                }
            }
            // If no image matching the desired size was found, return the first image
            return imageList.get(0).url;
        }
        return null;
    }

}
