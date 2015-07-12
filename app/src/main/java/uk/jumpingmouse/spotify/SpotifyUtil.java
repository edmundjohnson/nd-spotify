package uk.jumpingmouse.spotify;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Class containing Spotify-related utility methods.
 * @author Edmund Johnson
 */
public class SpotifyUtil {

    /** Private constructor to prevent instantiation. */
    private SpotifyUtil() {
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
    public static String getImageUrl(List<Image> imageList, int sizeMin, int sizeMax) {
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
