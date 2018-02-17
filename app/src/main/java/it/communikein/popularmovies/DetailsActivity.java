package it.communikein.popularmovies;

import android.content.ContentResolver;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;

import it.communikein.popularmovies.database.MoviesContract;
import it.communikein.popularmovies.databinding.ActivityDetailsBinding;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.utilities.ContentValuesHelper;

public class DetailsActivity extends AppCompatActivity {

    public static final String KEY_MOVIE = "MOVIE";

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
    }

    private void parseData() {
        Intent startIntent = getIntent();
        if (startIntent != null)
            mMovie = startIntent.getParcelableExtra(KEY_MOVIE);

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
