package me.rlxu.flixster.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Config {

    // base URL for loading images
    String imageBaseUrl;
    // the poster size when fetching images, part of URL
    String posterSize;
    // the backdrop size when fetching images
    String backdropSize;

    public Config(JSONObject object) throws JSONException {
        JSONObject images = object.getJSONObject("images");
        // get the image base URL
        imageBaseUrl = images.getString("secure_base_url");
        // get the poster size
        JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
        // use option at index 3 or w342 as fallback
        posterSize = posterSizeOptions.optString(3, "w342");
        // get the backdrop size
        JSONArray backdropSizeOptions = images.getJSONArray("backdrop_sizes");
        // use option at index 1 or w780 as fallback
        backdropSize = backdropSizeOptions.optString(1, "w780");
    }

    // helper method for creating URLs
    public String getImageUrl(String size, String path) {
        return String.format("%s%s%s", imageBaseUrl, size, path);
    }

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    public String getPosterSize() {
        return posterSize;
    }

    public String getBackdropSize() {
        return backdropSize;
    }
}
