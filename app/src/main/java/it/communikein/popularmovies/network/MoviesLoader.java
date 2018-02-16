package it.communikein.popularmovies.network;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;

import it.communikein.popularmovies.DatasetMovies;

public class MoviesLoader extends AsyncTaskLoader<DatasetMovies> {

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final boolean mPopular;
    private final int mPage;

    private MoviesLoader(Activity activity, boolean popular, int page) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mPopular = popular;
        this.mPage = page;
    }

    public static MoviesLoader createPopularMoviesLoader(Activity activity, int page) {
        return new MoviesLoader(activity, true, page);
    }

    public static MoviesLoader createTopRatedMoviesLoader(Activity activity, int page) {
        return new MoviesLoader(activity, false, page);
    }

    @Override
    public DatasetMovies loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            URL url = NetworkUtils.getMoviesUrl(mPopular, mPage);
            if (url == null)
                return null;

            Bundle response = NetworkUtils.getResponseFromHttpUrl(url);
            if (response.containsKey(NetworkUtils.KEY_DATA)) {
                Type type = new TypeToken<DatasetMovies>(){}.getType();
                return new Gson().fromJson(response.getString(NetworkUtils.KEY_DATA), type);
            }
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

}

