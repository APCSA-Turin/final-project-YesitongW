package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
public class MovieRecommender {

    private static final String API_KEY = "c25b9c21ce32ba7773e9dfa6f1ea46c3";

    public static List<String> getSimilarMovies(int movieId) throws Exception {

        String endpoint = "https://api.themoviedb.org/3/movie/" + movieId + "/similar?api_key=" + API_KEY;

        String json = API.getData(endpoint);
        JSONArray results = new JSONObject(json).getJSONArray("results");

        List<String> recs = new ArrayList<>();

        // Add no more than ten results to the list
        int limit = (results.length() < 10) ? results.length() : 10;

        for (int i = 0; i < limit; i++) {
            recs.add(results.getJSONObject(i).getString("title"));
        }
        return recs;
    }
}
