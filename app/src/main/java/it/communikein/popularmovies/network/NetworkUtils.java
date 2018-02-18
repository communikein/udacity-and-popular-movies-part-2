package it.communikein.popularmovies.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import it.communikein.popularmovies.offline.ApiKeyUtils;

public class NetworkUtils {

    private static final String API_BASE_URL = "https://api.themoviedb.org/3/movie";

    private static final String API_KEY_PARAM = "api_key";
    private static final String API_PAGE_PARAM = "page";

    private static final String API_VIDEOS_PATH = "videos";
    private static final String API_REVIEWS_PATH = "reviews";
    private static final String API_POPULAR_PATH = "popular";
    private static final String API_TOP_RATED_PATH = "top_rated";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_VIDEO_PARAM = "v";


    private static final String KEY_SERVER_RESPONSE = "SERVER_RESPONSE";
    public static final String KEY_DATA = "DATA";

    public static URL getMoviesUrl(boolean popular, int page) {
        Uri uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendPath(popular ? API_POPULAR_PATH : API_TOP_RATED_PATH)
                .appendQueryParameter(API_KEY_PARAM, ApiKeyUtils.API_KEY)
                .appendQueryParameter(API_PAGE_PARAM, String.valueOf(page))
                .build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URL getMovieVideosUrl(int movieId) {
        Uri uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(API_VIDEOS_PATH)
                .appendQueryParameter(API_KEY_PARAM, ApiKeyUtils.API_KEY)
                .build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static URL getMovieReviewsUrl(int movieId, int page) {
        Uri uri = Uri.parse(API_BASE_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(API_REVIEWS_PATH)
                .appendQueryParameter(API_KEY_PARAM, ApiKeyUtils.API_KEY)
                .appendQueryParameter(API_PAGE_PARAM, String.valueOf(page))
                .build();

        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Intent getYoutubeVideoIntent(String key) {
        Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_VIDEO_PARAM, key)
                .build();

        return new Intent(Intent.ACTION_VIEW, uri);
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

    public static boolean isDeviceOnline(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }
}
