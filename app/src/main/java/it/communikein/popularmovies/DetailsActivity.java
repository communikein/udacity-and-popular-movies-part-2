package it.communikein.popularmovies;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.communikein.popularmovies.database.MoviesContract;
import it.communikein.popularmovies.databinding.ActivityDetailsBinding;
import it.communikein.popularmovies.model.Dataset;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;
import it.communikein.popularmovies.model.Video;
import it.communikein.popularmovies.network.NetworkUtils;
import it.communikein.popularmovies.network.ReviewsLoader;
import it.communikein.popularmovies.network.VideosLoader;
import it.communikein.popularmovies.utilities.ContentValuesHelper;

import static it.communikein.popularmovies.database.MoviesContract.ReviewEntry;
import static it.communikein.popularmovies.database.MoviesContract.VideoEntry;

public class DetailsActivity extends AppCompatActivity implements
        VideosListAdapter.VideoClickCallback, LoaderManager.LoaderCallbacks,
        ReviewsListAdapter.ReviewClickCallback {

    public static final String KEY_MOVIE = "MOVIE";

    public static final int LOADER_VIDEOS_ID = 100;
    public static final int LOADER_REVIEWS_ID = 200;

    public static final String[] MOVIE_COLUMNS = new String[] {
            MoviesContract.MovieEntry.COLUMN_ID
    };
    public static final int INDEX_MOVIE_ID = 0;

    public static final String[] REVIEWS_COLUMNS = new String[] {
            ReviewEntry.COLUMN_ID,
            ReviewEntry.COLUMN_AUTHOR,
            ReviewEntry.COLUMN_CONTENT,
            ReviewEntry.COLUMN_URL
    };
    public static final int INDEX_REVIEW_ID = 0;
    public static final int INDEX_REVIEW_AUTHOR = 1;
    public static final int INDEX_REVIEW_CONTENT = 2;
    public static final int INDEX_REVIEW_URL = 3;

    public static final String[] VIDEOS_COLUMNS = new String[] {
            VideoEntry.COLUMN_ID,
            VideoEntry.COLUMN_KEY,
            VideoEntry.COLUMN_NAME,
            VideoEntry.COLUMN_WEBSITE
    };
    public static final int INDEX_VIDEO_ID = 0;
    public static final int INDEX_VIDEO_KEY = 1;
    public static final int INDEX_VIDEO_NAME = 2;
    public static final int INDEX_VIDEO_WEBSITE = 3;

    public interface FavouriteMovieUpdateListener {
        void onFavouriteMovieUpdated(Movie movie);
    }

    private ActivityDetailsBinding mBinding;

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        parseData();
        initToolbar();
        initFab();

        initVideosList();
        initReviewsList();

        if (mMovie.getVideos() == null || mMovie.getReviews() == null)
            loadReviewsVideos();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_MOVIE, mMovie);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(KEY_MOVIE))
            mMovie = savedInstanceState.getParcelable(KEY_MOVIE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_details_menu, menu);

        return true;
    }

    private void parseData() {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }

        mMovie = startIntent.getParcelableExtra(KEY_MOVIE);

        checkIfFavourite();

        mBinding.titleTextview.setText(mMovie.getOriginalTitle());
        mBinding.descriptionTextview.setText(mMovie.getOverview());
        mBinding.voteAverageTextview.setText(
                getString(R.string.average_vote, mMovie.printVoteAverage()));
        mBinding.releaseDateTextview.setText(mMovie.printReleaseDate());

        Picasso.with(this)
                .load(mMovie.getPosterFullPath())
                .error(R.drawable.ic_broken_image_white)
                .placeholder(R.drawable.ic_image_black_white)
                .into(mBinding.moviePosterImageview);
    }

    private void checkIfFavourite() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Cursor cursor = getContentResolver().query(
                    MoviesContract.MovieEntry.buildMovieUri(mMovie.getId()),
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);

            mMovie.setFavourite(parseMovieCursor(cursor) != -1);

            AppExecutors.getInstance().mainThread().execute(() -> updateFab(mMovie));
        });
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Picasso.with(this)
                    .load(mMovie.getPosterFullPath())
                    .error(R.drawable.ic_broken_image_white)
                    .placeholder(R.drawable.ic_image_black_white)
                    .into(mBinding.image);

            mBinding.collapsingToolbar.setTitle(" ");
            mBinding.collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
            mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        mBinding.collapsingToolbar.setTitle(mMovie.getOriginalTitle());
                        mBinding.image.setAlpha(.7f);
                        isShow = true;
                    } else if (isShow) {
                        // There should a space between double quote otherwise it won't work
                        mBinding.collapsingToolbar.setTitle(" ");
                        mBinding.image.setAlpha(1f);
                        isShow = false;
                    }
                }
            });
        }
        else if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(mMovie.getOriginalTitle());
    }

    private void initFab() {
        updateFab(mMovie);

        mBinding.favoriteFab.setOnClickListener(v -> {
            updateFavourite(mMovie, movie -> AppExecutors.getInstance().mainThread().execute(() -> {
                updateFab(movie);

                if (movie.isFavourite())
                    Snackbar.make(mBinding.coordinatorView, R.string.label_movie_added_to_favourites,
                            Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mBinding.coordinatorView, R.string.label_movie_removed_from_favourites,
                            Snackbar.LENGTH_LONG).show();
            }));
        });
    }

    private void updateFab(Movie movie) {
        mBinding.favoriteFab.setEnabled(movie.getReviews() != null && movie.getVideos() != null);

        if (movie.isFavourite())
            mBinding.favoriteFab.setImageResource(R.drawable.ic_star_border_white);
        else
            mBinding.favoriteFab.setImageResource(R.drawable.ic_star_white);
    }

    private void initVideosList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);
        mBinding.videosList.setLayoutManager(layoutManager);
        mBinding.videosList.setHasFixedSize(true);

        VideosListAdapter videosListAdapter = new VideosListAdapter(this);
        mBinding.videosList.setAdapter(videosListAdapter);

        if (mMovie.getVideos() != null) {
            videosListAdapter.setList(mMovie.getVideos());
            videosListAdapter.notifyDataSetChanged();
        }
    }

    private void loadReviewsVideos() {
        if (mMovie.isFavourite()) {
            Cursor cursor = getContentResolver().query(
                    MoviesContract.ReviewEntry.buildMovieReviewsUri(mMovie.getId()),
                    REVIEWS_COLUMNS,
                    null,
                    null,
                    null);
            mMovie.setReviews(parseReviewsCursor(cursor));
            if (cursor != null) cursor.close();

            if (mMovie.getReviews().size() == 0) {
                mBinding.labelReviews.setVisibility(View.GONE);
                mBinding.reviewsList.setVisibility(View.GONE);
            }
            else {
                mBinding.labelReviews.setVisibility(View.VISIBLE);
                mBinding.reviewsList.setVisibility(View.VISIBLE);

                ReviewsListAdapter reviewsAdapter = (ReviewsListAdapter) mBinding.reviewsList.getAdapter();
                reviewsAdapter.setList(mMovie.getReviews());
                reviewsAdapter.notifyDataSetChanged();
            }

            cursor = getContentResolver().query(
                    MoviesContract.VideoEntry.buildMovieVideosUri(mMovie.getId()),
                    VIDEOS_COLUMNS,
                    null,
                    null,
                    null);
            mMovie.setVideos(parseVideosCursor(cursor));
            if (cursor != null) cursor.close();

            if (mMovie.getVideos().size() == 0) {
                mBinding.labelVideos.setVisibility(View.GONE);
                mBinding.videosList.setVisibility(View.GONE);
            }
            else {
                mBinding.labelVideos.setVisibility(View.VISIBLE);
                mBinding.videosList.setVisibility(View.VISIBLE);

                VideosListAdapter videoAdapter = (VideosListAdapter) mBinding.videosList.getAdapter();
                videoAdapter.setList(mMovie.getVideos());
                videoAdapter.notifyDataSetChanged();
            }

            updateFab(mMovie);
        }
        else {
            startLoader(LOADER_VIDEOS_ID);
            startLoader(LOADER_REVIEWS_ID);
        }
    }

    @Override
    public void onListVideoClick(Video video) {
        Intent youtubeIntent = NetworkUtils.getYoutubeVideoIntent(video.getKey());
        startActivity(youtubeIntent);
    }

    private void initReviewsList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);
        mBinding.reviewsList.setLayoutManager(layoutManager);
        mBinding.reviewsList.setHasFixedSize(true);

        ReviewsListAdapter reviewsListAdapter = new ReviewsListAdapter(this);
        mBinding.reviewsList.setAdapter(reviewsListAdapter);

        if (mMovie.getReviews() != null) {
            reviewsListAdapter.setList(mMovie.getReviews());
            reviewsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onReviewClick(Review review) {
        ShowReviewDialog dialog = new ShowReviewDialog()
                .setReview(review);

        dialog.setCancelable(true);
        dialog.show(getSupportFragmentManager(), ShowReviewDialog.class.getSimpleName());
    }


    private void updateFavourite(Movie movie, FavouriteMovieUpdateListener listener) {
        ContentResolver contentResolver = getContentResolver();
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (movie.isFavourite()) {
                contentResolver.delete(
                        MoviesContract.MovieEntry.buildMovieUri(movie.getId()),
                        null,
                        null);
            }
            else {
                contentResolver.insert(
                        MoviesContract.MovieEntry.buildMovieUri(movie.getId()),
                        ContentValuesHelper.toContentValues(movie));

                contentResolver.bulkInsert(
                        MoviesContract.ReviewEntry.buildMovieReviewsUri(movie.getId()),
                        ContentValuesHelper.toReviewArrayContentValues(movie.getReviews()));

                contentResolver.bulkInsert(
                        MoviesContract.VideoEntry.buildMovieVideosUri(movie.getId()),
                        ContentValuesHelper.toVideoArrayContentValues(movie.getVideos()));
            }

            movie.setFavourite(!movie.isFavourite());
            listener.onFavouriteMovieUpdated(movie);
        });
    }


    private void startLoader(int loaderId) {
        if (mMovie.isFavourite() || NetworkUtils.isDeviceOnline(this))
            getSupportLoaderManager()
                    .restartLoader(loaderId, null, DetailsActivity.this)
                    .forceLoad();
        else
            Snackbar.make(mBinding.coordinatorView, R.string.error_no_internet, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> startLoader(loaderId))
                    .show();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_VIDEOS_ID:
                return VideosLoader.createVideosLoader(this, mMovie);

            case LOADER_REVIEWS_ID:
                return ReviewsLoader.createReviewsLoader(this, mMovie, 1);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()) {
            case LOADER_VIDEOS_ID:
                List<Video> videos = (List<Video>) data;
                mMovie.setVideos(videos);

                updateFab(mMovie);
                if (videos.size() == 0) {
                    mBinding.labelVideos.setVisibility(View.GONE);
                    mBinding.videosList.setVisibility(View.GONE);
                }
                else {
                    mBinding.labelVideos.setVisibility(View.VISIBLE);
                    mBinding.videosList.setVisibility(View.VISIBLE);

                    VideosListAdapter videoAdapter = (VideosListAdapter) mBinding.videosList.getAdapter();
                    videoAdapter.setList(videos);
                    videoAdapter.notifyDataSetChanged();

                    if (mMovie.isFavourite())
                        getContentResolver().bulkInsert(
                                MoviesContract.VideoEntry.CONTENT_URI,
                                ContentValuesHelper.toVideoArrayContentValues(videos));
                }
                break;

            case LOADER_REVIEWS_ID:
                Dataset<Review> reviews = (Dataset<Review>) data;
                mMovie.setReviews(reviews.getResults());

                updateFab(mMovie);
                if (reviews.getResults().size() == 0) {
                    mBinding.labelReviews.setVisibility(View.GONE);
                    mBinding.reviewsList.setVisibility(View.GONE);
                }
                else {
                    mBinding.labelReviews.setVisibility(View.VISIBLE);
                    mBinding.reviewsList.setVisibility(View.VISIBLE);

                    ReviewsListAdapter reviewsAdapter = (ReviewsListAdapter) mBinding.reviewsList.getAdapter();
                    reviewsAdapter.setList(reviews.getResults());
                    reviewsAdapter.notifyDataSetChanged();

                    if (mMovie.isFavourite())
                        getContentResolver().bulkInsert(
                                MoviesContract.ReviewEntry.CONTENT_URI,
                                ContentValuesHelper.toReviewArrayContentValues(reviews.getResults()));
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) { }


    private List<Review> parseReviewsCursor(Cursor cursor) {
        List<Review> reviews = new ArrayList<>();

        if (cursor != null) for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            String id = cursor.getString(INDEX_REVIEW_ID);
            String author = cursor.getString(INDEX_REVIEW_AUTHOR);
            String content = cursor.getString(INDEX_REVIEW_CONTENT);
            String url = cursor.getString(INDEX_REVIEW_URL);

            Review review = new Review(id, author, content, url, mMovie.getId());
            reviews.add(review);
        }

        return reviews;
    }

    private List<Video> parseVideosCursor(Cursor cursor) {
        List<Video> videos = new ArrayList<>();

        if (cursor != null) for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            String id = cursor.getString(INDEX_VIDEO_ID);
            String key = cursor.getString(INDEX_VIDEO_KEY);
            String name = cursor.getString(INDEX_VIDEO_NAME);
            String website  = cursor.getString(INDEX_VIDEO_WEBSITE);

            Video video = new Video(id, key, name, website, mMovie.getId());
            videos.add(video);
        }

        return videos;
    }

    private int parseMovieCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() != 1) return -1;

        cursor.moveToFirst();
        return cursor.getInt(INDEX_MOVIE_ID);
    }

    private Intent createShareIntent(Movie movie) {
        Video video = movie.getVideos().get(0);
        String uri = NetworkUtils.getYoutubeVideoIntent(video.getKey()).toUri(0);

        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.share_video, movie.getOriginalTitle()))
                .append("\n").append(uri);

        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(message)
                .getIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:
                if (mMovie.getVideos() != null && mMovie.getVideos().size() > 0){
                    Intent shareIntent = createShareIntent(mMovie);
                    startActivity(shareIntent);
                }
                else {
                    Snackbar.make(mBinding.coordinatorView,
                            R.string.error_no_video_available,
                            Snackbar.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
