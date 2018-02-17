package it.communikein.popularmovies.network;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;

import it.communikein.popularmovies.model.Dataset;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;

public class ReviewsLoader extends AsyncTaskLoader<Dataset<Review>> {

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final Movie mMovie;
    private final int mPage;

    private ReviewsLoader(Activity activity, Movie movie, int page) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mMovie = movie;
        this.mPage = page;
    }

    public static ReviewsLoader createReviewsLoader(Activity activity, Movie movie, int page) {
        return new ReviewsLoader(activity, movie, page);
    }

    @Override
    public Dataset<Review> loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            URL url = NetworkUtils.getMovieReviewsUrl(mMovie.getId(), mPage);
            if (url == null)
                return null;

            Bundle response = NetworkUtils.getResponseFromHttpUrl(url);
            if (response.containsKey(NetworkUtils.KEY_DATA)) {
                Type type = new TypeToken<Dataset<Review>>(){}.getType();
                return new Gson().fromJson(response.getString(NetworkUtils.KEY_DATA), type);
            }
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

}
