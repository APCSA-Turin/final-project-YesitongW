package com.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Main {

    // Your TMDb API key
    private static final String API_KEY = "c25b9c21ce32ba7773e9dfa6f1ea46c3";

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter movie title: ");
        String titleInput = scanner.nextLine().trim();

        // Resolve user-supplied title to a TMDb numeric ID.
        int movieId = MovieSearcher.getMovieId(titleInput);
        if (movieId == -1) {
            System.out.println("Sorry, that title was not found.");
            scanner.close();
            return; 
        }

        // Pull recommendations and supporting genre data.
        List<Movie> recommendations = fetchRecommendations(movieId);
        Map<Integer, String> genreMap = fetchGenreMap();


        System.out.print("Filter by genre (leave blank for any): ");
        String wantedGenre = scanner.nextLine().trim();

        System.out.print("Start year (0 for any): ");
        int fromYear = safeParseInt(scanner.nextLine(), 0);

        System.out.print("End year (0 for any): ");
        int toYear = safeParseInt(scanner.nextLine(), 0);

        scanner.close();


        List<Movie> filtered = filterMovies(recommendations, fromYear, toYear, wantedGenre, genreMap);

        // Sort so that higher scores come first.
        Collections.sort(filtered, new Comparator<Movie>() {
            @Override
            public int compare(Movie a, Movie b) {
                return Double.compare(b.getScore(), a.getScore()); // higher first
            }
        });

        if (filtered.isEmpty()) {
            System.out.println("\nNo movies matched your criteria.");
        } else {
            System.out.println("\nTop picks:");
            for (int rank = 0; rank < filtered.size() && rank < 10; rank++) {
                System.out.println((rank + 1) + ". " + filtered.get(rank).toSimpleString());
            }
        }
    }

    private static int safeParseInt(String text, int fallback) {
        String s = text.trim();
        if (s.isEmpty()) {
            return fallback;
        }
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') { // non-digit
                return fallback;
            }
            num = num * 10 + (c - '0');
        }
        return num;
    }

    // Query TMDb for recommended titles related to movieId
    private static List<Movie> fetchRecommendations(int movieId) throws Exception {
        String endpoint = "https://api.themoviedb.org/3/movie/" + movieId + "/recommendations?api_key=" + API_KEY;

        String json = API.getData(endpoint);
        JSONArray results = new JSONObject(json).getJSONArray("results");

        List<Movie> movies = new ArrayList<>();

        // Build a Movie object for every result
        for (int i = 0; i < results.length(); i++) {
            JSONObject m = results.getJSONObject(i);
            movies.add(new Movie(m.getInt("id"),m.getString("title"), m.optString("release_date", ""), m.optDouble("vote_average", 0.0),m.optDouble("popularity", 0.0), m.getJSONArray("genre_ids")));
        }
        return movies;
    }

    // Download TMDb genre map
    private static Map<Integer, String> fetchGenreMap() throws Exception {
        String endpoint = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY;

        String json = API.getData(endpoint);
        JSONArray genres = new JSONObject(json).getJSONArray("genres");

        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < genres.length(); i++) {
            JSONObject g = genres.getJSONObject(i);
            map.put(g.getInt("id"), g.getString("name"));
        }
        return map;
    }

    // Apply year and genre filters
    private static List<Movie> filterMovies(List<Movie> movies, int fromYear, int toYear, String genre, Map<Integer, String> gm) {

        boolean needGenreCheck = genre != null && !genre.isEmpty();
        List<Movie> out = new ArrayList<>();
        for (Movie m : movies) {
            int yr = m.getYear();
            boolean yearOk = (fromYear == 0 || yr >= fromYear) && (toYear == 0 || yr <= toYear);
            boolean genreOk = !needGenreCheck || m.hasGenre(genre, gm);

            if (yearOk && genreOk) {
                out.add(m);
            }
        }
        return out;
    }

    private static final class Movie {

        private final int id;
        private final String title;
        private final String releaseDate; // ISO yyyy-MM-dd or empty
        private final double rating;      // TMDb vote_average
        private final double popularity;  // TMDb popularity
        private final List<Integer> genreIds = new ArrayList<>();

        Movie(int id, String title, String releaseDate, double rating, double popularity, JSONArray genreIds) {

            this.id = id;
            this.title = title;

            this.releaseDate = (releaseDate == null) ? "" : releaseDate;

            this.rating = rating;
            this.popularity = popularity;

            // Convert JSONArray → List<Integer>
            for (int i = 0; i < genreIds.length(); i++) {
                this.genreIds.add(genreIds.getInt(i));
            }
        }

        int getYear() {
            if (releaseDate.length() < 4) {
                return 0;
            }
            return safeParseInt(releaseDate.substring(0, 4), 0);
        }

        // True if this movie belongs to the named genre
        boolean hasGenre(String name, Map<Integer, String> map) {
            for (int gid : genreIds) {
                String g = map.get(gid);
                if (g != null && g.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        double getScore() {
            return rating * 10.0 + popularity;
        }

        String toSimpleString() {
        String yearText;
            if (releaseDate.length() >= 4) {
                yearText = releaseDate.substring(0, 4);
            } else {
                yearText = "Unknown";
            }
        return title + " (" + yearText + ")  rating=" + rating + ", popularity=" + (int) popularity;
        }
    }
}
