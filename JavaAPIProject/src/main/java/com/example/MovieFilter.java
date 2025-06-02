package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieFilter {

    private static final String API_KEY = "c25b9c21ce32ba7773e9dfa6f1ea46c3";

    public static List<String> filterMovies(String genreId, String startDate,  String endDate) throws Exception {

        // Build Discover endpoint.
        String endpoint ="https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&with_genres=" + genreId + "&primary_release_date.gte=" + startDate + "&primary_release_date.lte=" + endDate;
        String json = API.getData(endpoint);
        JSONArray results = new JSONObject(json).getJSONArray("results");

        List<String> titles = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            titles.add(results.getJSONObject(i).getString("title"));
        }
        return titles;
    }
}
