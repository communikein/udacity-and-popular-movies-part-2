package it.communikein.popularmovies.database;

import android.net.Uri;

import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;
import it.communikein.popularmovies.model.Video;

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "it.communikein.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_VIDEOS = "videos";

    public static final class MovieEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_ID = Movie.ID;
        public static final String COLUMN_VOTE_AVERAGE = Movie.VOTE_AVERAGE;
        public static final String COLUMN_POSTER_PATH = Movie.POSTER_PATH;
        public static final String COLUMN_ORIGINAL_TITLE = Movie.ORIGINAL_TITLE;
        public static final String COLUMN_OVERVIEW = Movie.OVERVIEW;
        public static final String COLUMN_RELEASE_DATE = Movie.RELEASE_DATE;
        
        public static Uri buildMovieUri(int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }

    public static final class ReviewEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS)
                .build();

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_ID = Review.ID;
        public static final String COLUMN_AUTHOR = Review.AUTHOR;
        public static final String COLUMN_CONTENT = Review.CONTENT;
        public static final String COLUMN_URL = Review.URL;
        public static final String COLUMN_MOVIE_ID = Review.MOVIE_ID;

        public static Uri buildMovieReviewsUri(int movie_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movie_id))
                    .build();
        }
    }

    public static final class VideoEntry {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_VIDEOS)
                .build();

        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_ID = Video.ID;
        public static final String COLUMN_KEY = "video_" + Video.KEY;
        public static final String COLUMN_NAME = Video.NAME;
        public static final String COLUMN_WEBSITE = Video.WEBSITE;
        public static final String COLUMN_MOVIE_ID = Video.MOVIE_ID;

        public static Uri buildMovieVideosUri(int movie_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movie_id))
                    .build();
        }
    }

}
