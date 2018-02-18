package it.communikein.popularmovies;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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
import it.communikein.popularmovies.network.MoviesLoader;
import it.communikein.popularmovies.network.NetworkUtils;
import it.communikein.popularmovies.network.ReviewsLoader;
import it.communikein.popularmovies.network.VideosLoader;
import it.communikein.popularmovies.utilities.ContentValuesHelper;

public class DetailsActivity extends AppCompatActivity implements
        VideosListAdapter.VideoClickCallback, LoaderManager.LoaderCallbacks, ReviewsListAdapter.ReviewClickCallback {

    public static final String KEY_MOVIE = "MOVIE";

    public static final int LOADER_VIDEOS_ID = 100;
    public static final int LOADER_REVIEWS_ID = 200;


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
    }

    private void parseData() {
        Intent startIntent = getIntent();
        if (startIntent == null) {
            finish();
            return;
        }

        mMovie = startIntent.getParcelableExtra(KEY_MOVIE);

        startLoader(LOADER_VIDEOS_ID);
        startLoader(LOADER_REVIEWS_ID);

        mBinding.titleTextview.setText(mMovie.getOriginalTitle());
        mBinding.descriptionTextview.setText(mMovie.getOverview());
        mBinding.voteAverageTextview.setText(
                getString(R.string.average_vote, mMovie.printVoteAverage()));
        mBinding.releaseDateTextview.setText(mMovie.printReleaseDate());

        Picasso.with(this)
                .load(mMovie.getPosterFullPath())
                .into(mBinding.moviePosterImageview);
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Picasso.with(this)
                .load(mMovie.getPosterFullPath())
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
                } else if(isShow) {
                    // There should a space between double quote otherwise it won't work
                    mBinding.collapsingToolbar.setTitle(" ");
                    mBinding.image.setAlpha(1f);
                    isShow = false;
                }
            }
        });
    }

    private void initFab() {
        updateFabIcon(mMovie);

        mBinding.favoriteFab.setOnClickListener(v -> {
            updateFavourite(mMovie, movie -> AppExecutors.getInstance().mainThread().execute(() -> {
                updateFabIcon(movie);
                if (movie.isFavourite())
                    Snackbar.make(mBinding.coordinatorView, R.string.label_movie_added_to_favourites,
                            Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mBinding.coordinatorView, R.string.label_movie_removed_from_favourites,
                            Snackbar.LENGTH_LONG).show();
            }));
        });
    }

    private void updateFabIcon(Movie movie) {
        mBinding.favoriteFab.setEnabled(true);
        if (movie.isFavourite())
            mBinding.favoriteFab.setImageResource(R.drawable.ic_star_white);
        else
            mBinding.favoriteFab.setImageResource(R.drawable.ic_star_border_white);
    }

    private void initVideosList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false);
        mBinding.videosList.setLayoutManager(layoutManager);
        mBinding.videosList.setHasFixedSize(true);

        VideosListAdapter videosListAdapter = new VideosListAdapter(this);
        mBinding.videosList.setAdapter(videosListAdapter);
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
    }

    @Override
    public void onReviewClick(Review review) {
        ShowReviewDialog dialog = new ShowReviewDialog()
                .setReview(review);

        dialog.setCancelable(true);
        dialog.show(getSupportFragmentManager(), ShowReviewDialog.class.getSimpleName());
    }


    private void updateFavourite(Movie movie, FavouriteMovieUpdateListener listener) {
        mBinding.favoriteFab.setEnabled(false);

        ContentResolver contentResolver = getContentResolver();
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (movie.isFavourite())
                contentResolver.delete(
                        MoviesContract.MovieEntry.buildMovieUri(movie.getId()),
                        null,
                        null);
            else
                contentResolver.insert(
                        MoviesContract.MovieEntry.buildMovieUri(movie.getId()),
                        ContentValuesHelper.toContentValues(movie));

            movie.setFavourite(!movie.isFavourite());
            listener.onFavouriteMovieUpdated(movie);
        });
    }


    private void startLoader(int loaderId) {
        // TODO: Remember to check if the movie is favourite

        if (NetworkUtils.isDeviceOnline(this))
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
                }
                break;

            case LOADER_REVIEWS_ID:
                Dataset<Review> reviews = (Dataset<Review>) data;
                mMovie.setReviews(reviews.getResults());

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
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) { }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
