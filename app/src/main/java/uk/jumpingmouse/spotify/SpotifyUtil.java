package uk.jumpingmouse.spotify;

import java.util.List;

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
     * Returns a URL for a small image from a list of image objects.
     * @param imageList the list of images
     * @return The URL for a small image from the list is returned if found,
     *         otherwise the URL for any image is returned.
     *         If no images are found, null is returned.
     */
    public static String getSmallImageUrl(List<Image> imageList) {
        return getImageUrl(imageList, SMALL_IMAGE_SIZE_MIN, SMALL_IMAGE_SIZE_MAX);
    }

    /**
     * Returns a URL for a large image from a list of image objects.
     * @param imageList the list of images
     * @return The URL for a large image from the list is returned if found,
     *         otherwise the URL for any image is returned.
     *         If no images are found, null is returned.
     */
    public static String getLargeImageUrl(List<Image> imageList) {
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

    /**
     * Returns the preview URL for a Spotify track.
     * @param track the Spotify track
     * @return the preview URL for the Spotify track
     */
    public static String getPreviewUrl(Track track) {
        return track == null ? null : track.preview_url;

    }
}
