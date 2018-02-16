package it.communikein.popularmovies.network;

import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

class NetworkUtils {

    private static final String API_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String API_KEY_PARAM = "?api_key=";

    private static final String URL_POPULAR_MOVIE = API_BASE_URL + "/popular" + API_KEY_PARAM +
            ApiKeyUtils.API_KEY;
    private static final String URL_TOP_RATED_MOVIE = API_BASE_URL + "/top_rated" + API_KEY_PARAM +
            ApiKeyUtils.API_KEY;

    private static final String KEY_SERVER_RESPONSE = "SERVER_RESPONSE";
    public static final String KEY_DATA = "DATA";

    public static URL getMoviesUrl(boolean popular, int page) {
        try {
            URL url;
            if (popular)
                url = new URL(URL_POPULAR_MOVIE + "&page=" + String.valueOf(page));
            else
                url = new URL(URL_TOP_RATED_MOVIE + "&page=" + String.valueOf(page));

            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bundle getResponseFromHttpUrl(URL url) throws IOException {
        Bundle data = new Bundle();

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == 200) {
            try {
                InputStream in = urlConnection.getInputStream();

                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                String response = null;
                if (hasInput) {
                    response = scanner.next();
                }
                scanner.close();

                data.putString(KEY_DATA, response);
            } finally {
                urlConnection.disconnect();
            }
        }
        data.putInt(KEY_SERVER_RESPONSE, responseCode);

        return data;
    }
}
