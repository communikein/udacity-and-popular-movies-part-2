package it.communikein.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.communikein.popularmovies.database.MoviesContract.MovieEntry;
import it.communikein.popularmovies.database.MoviesContract.ReviewEntry;
import it.communikein.popularmovies.database.MoviesContract.VideoEntry;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;

public class MoviesDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MoviesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry.COLUMN_ID               + " INTEGER PRIMARY KEY, " +
                        MovieEntry.COLUMN_ORIGINAL_TITLE   + " TEXT NOT NULL, "       +
                        MovieEntry.COLUMN_OVERVIEW         + " TEXT NOT NULL, "       +
                        MovieEntry.COLUMN_POSTER_PATH      + " TEXT NOT NULL, "       +
                        MovieEntry.COLUMN_RELEASE_DATE     + " INTEGER NOT NULL, "    +
                        MovieEntry.COLUMN_VOTE_AVERAGE     + " REAL NOT NULL);";

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                        ReviewEntry.COLUMN_ID              + " TEXT PRIMARY KEY, "    +
                        ReviewEntry.COLUMN_AUTHOR          + " TEXT NOT NULL, "       +
                        ReviewEntry.COLUMN_CONTENT         + " TEXT NOT NULL, "       +
                        ReviewEntry.COLUMN_URL             + " TEXT NOT NULL, "       +
                        ReviewEntry.COLUMN_MOVIE_ID        + " INTEGER NOT NULL);";

        final String SQL_CREATE_VIDEOS_TABLE =
                "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                        VideoEntry.COLUMN_ID               + " TEXT PRIMARY KEY, "    +
                        VideoEntry.COLUMN_KEY              + " TEXT NOT NULL, "       +
                        VideoEntry.COLUMN_NAME             + " TEXT NOT NULL, "       +
                        VideoEntry.COLUMN_WEBSITE          + " TEXT NOT NULL, "       +
                        VideoEntry.COLUMN_MOVIE_ID         + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
