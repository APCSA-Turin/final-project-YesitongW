package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

// Convert a human-readable movie title into its TMDb numeric ID.
// Returns -1 if no match is found.
public class MovieSearcher {

    private static final String API_KEY = "c25b9c21ce32ba7773e9dfa6f1ea46c3";

    public static int getMovieId(String title) throws Exception {

        String query = title.replace(" ", "%20");

        String endpoint = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&query=" + query;

        String json = API.getData(endpoint);
        JSONArray results = new JSONObject(json).getJSONArray("results");

        if (results.length() == 0) {
            return -1; // nothing found
        }

        // The first element is usually the best match.
        JSONObject first = results.getJSONObject(0);
        return first.getInt("id");
    }
}

