package it.communikein.popularmovies.network;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import it.communikein.popularmovies.model.Dataset;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Video;

public class VideosLoader extends AsyncTaskLoader<List<Video>> {

    // Weak references will still allow the Context to be garbage-collected
    private final WeakReference<Activity> mActivity;
    private final Movie mMovie;

    private VideosLoader(Activity activity, Movie movie) {
        super(activity);

        this.mActivity = new WeakReference<>(activity);
        this.mMovie = movie;
    }

    public static VideosLoader createVideosLoader(Activity activity, Movie movie) {
        return new VideosLoader(activity, movie);
    }

    @Override
    public List<Video> loadInBackground() {
        Activity context = mActivity.get();
        if (context == null) return null;

        try {
            URL url = NetworkUtils.getMovieVideosUrl(mMovie.getId());
            if (url == null)
                return null;

            Bundle response = NetworkUtils.getResponseFromHttpUrl(url);
            if (response.containsKey(NetworkUtils.KEY_DATA)) {
                Type type = new TypeToken<List<Video>>(){}.getType();

                JSONObject obj = new JSONObject(response.getString(NetworkUtils.KEY_DATA));
                if (obj.has(Dataset.RESULTS)) {
                    List<Video> videos = new Gson().fromJson(obj.getJSONArray(Dataset.RESULTS).toString(), type);
                    for (Video video : videos)
                        video.setMovieId(mMovie.getId());

                    return videos;
                }
                else
                    return null;
            }
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
