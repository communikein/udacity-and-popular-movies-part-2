package it.communikein.popularmovies.utilities;

import android.content.ContentValues;

import it.communikein.popularmovies.model.Movie;

public class ContentValuesHelper {

    public static ContentValues toContentValues(Movie movie) {
        ContentValues values = new ContentValues();

        values.put(Movie.ID, movie.getId());
        values.put(Movie.VOTE_AVERAGE, movie.getVoteAverage());
        values.put(Movie.POSTER_PATH, movie.getPosterPath());
        values.put(Movie.ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(Movie.OVERVIEW, movie.getOverview());
        values.put(Movie.RELEASE_DATE, movie.getReleaseDate().getTime());

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
