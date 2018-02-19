package it.communikein.popularmovies.utilities;

import android.content.ContentValues;

import java.util.List;

import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;
import it.communikein.popularmovies.model.Video;

import it.communikein.popularmovies.database.MoviesContract.VideoEntry;
import it.communikein.popularmovies.database.MoviesContract.MovieEntry;
import it.communikein.popularmovies.database.MoviesContract.ReviewEntry;

public class ContentValuesHelper {

    public static ContentValues toContentValues(Movie movie) {
        ContentValues values = new ContentValues();

        values.put(MovieEntry.COLUMN_ID, movie.getId());
        values.put(MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate().getTime());

        return values;
    }

    public static ContentValues toContentValues(Review review) {
        ContentValues values = new ContentValues();

        values.put(ReviewEntry.COLUMN_ID, review.getId());
        values.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
        values.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
        values.put(ReviewEntry.COLUMN_URL, review.getUrl());
        values.put(ReviewEntry.COLUMN_MOVIE_ID, review.getMovieId());

        return values;
    }

    public static ContentValues[] toReviewArrayContentValues(List<Review> reviews) {
        ContentValues[] values = new ContentValues[reviews.size()];

        int i = 0;
        for (Review review : reviews) {
            values[i] = toContentValues(review);
            i++;
        }

        return values;
    }

    public static ContentValues toContentValues(Video video) {
        ContentValues values = new ContentValues();

        values.put(VideoEntry.COLUMN_ID, video.getId());
        values.put(VideoEntry.COLUMN_KEY, video.getKey());
        values.put(VideoEntry.COLUMN_MOVIE_ID, video.getMovieId());
        values.put(VideoEntry.COLUMN_NAME, video.getName());
        values.put(VideoEntry.COLUMN_WEBSITE, video.getWebsite());

        return values;
    }

    public static ContentValues[] toVideoArrayContentValues(List<Video> videos) {
        ContentValues[] values = new ContentValues[videos.size()];

        int i = 0;
        for (Video video : videos) {
            values[i] = toContentValues(video);
            i++;
        }

        return values;
    }

    public static Movie toMovie(ContentValues values) {
        int id = -1;
        if (values.containsKey(Movie.ID))
            id = values.getAsInteger(Movie.ID);

        float voteAverage = -1;
        if (values.containsKey(Movie.VOTE_AVERAGE))
            voteAverage = values.getAsFloat(Movie.VOTE_AVERAGE);

        String posterPath = null;
        if (values.containsKey(Movie.POSTER_PATH))
            posterPath = values.getAsString(Movie.POSTER_PATH);

        String originalTitle = null;
        if (values.containsKey(Movie.ORIGINAL_TITLE))
            originalTitle = values.getAsString(Movie.ORIGINAL_TITLE);

        String overview = null;
        if (values.containsKey(Movie.OVERVIEW))
            overview = values.getAsString(Movie.OVERVIEW);

        long releaseDate = -1;
        if (values.containsKey(Movie.RELEASE_DATE))
            releaseDate = values.getAsLong(Movie.RELEASE_DATE);

        return new Movie(id, voteAverage, posterPath, originalTitle, overview, releaseDate);
    }


}
