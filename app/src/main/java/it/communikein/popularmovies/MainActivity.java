package it.communikein.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import it.communikein.popularmovies.database.MoviesContract;
import it.communikein.popularmovies.model.Dataset;
import it.communikein.popularmovies.databinding.ActivityMainBinding;
import it.communikein.popularmovies.model.Movie;
import it.communikein.popularmovies.model.Review;
import it.communikein.popularmovies.network.MoviesLoader;
import it.communikein.popularmovies.network.NetworkUtils;
import it.communikein.popularmovies.database.MoviesContract.MovieEntry;
import it.communikein.popularmovies.database.MoviesContract.ReviewEntry;
import it.communikein.popularmovies.database.MoviesContract.VideoEntry;


public class MainActivity extends AppCompatActivity implements
        MoviesGridAdapter.MovieClickCallback, LoaderManager.LoaderCallbacks,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String KEY_DATASET = "DATASET";
    private static final String KEY_FIRST_VISIBLE_ITEM_POS = "FIRST_VISIBLE_ITEM_POS";
    private static final String KEY_SELECTED_TAB = "SELECTED_TAB";

    private static final int LOADER_POPULAR_MOVIES_ID = 1001;
    private static final int LOADER_TOP_RATED_MOVIES_ID = 1002;
    private static final int LOADER_FAVOURITE_MOVIES_ID = 1003;

    private static final int LOADER_MORE_POPULAR_MOVIES_ID = 1011;
    private static final int LOADER_MORE_TOP_RATED_MOVIES_ID = 1012;
    private static final int LOADER_MORE_FAVOURITE_MOVIES_ID = 1013;

    public static final String[] MAIN_MOVIES_PROJECTION = {
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ID,
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_RELEASE_DATE
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 1;
    public static final int INDEX_MOVIE_POSTER_PATH = 2;
    public static final int INDEX_MOVIE_ORIGINAL_TITLE = 3;
    public static final int INDEX_MOVIE_OVERVIEW = 4;
    public static final int INDEX_MOVIE_RELEASE_DATE = 5;

    private ActivityMainBinding mBinding;

    private Dataset<Movie> moviesDataset;

    private static final int TAB_POPULAR_MOVIES = 0;
    private static final int TAB_TOP_RATED_MOVIES = 1;
    private static final int TAB_FAVOURITE_MOVIES = 2;
    private int selectedTab = TAB_POPULAR_MOVIES;

    private int lastItemPosition = -1;
    private int firstVisibleItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        /* Show data downloading */
        mBinding.swipeRefresh.setOnRefreshListener(this);
        mBinding.swipeRefresh.setRefreshing(false);

        initTabs();
        initGrid();
        initData(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hideProgressBar();
        if (savedInstanceState == null) return;
        if (!savedInstanceState.containsKey(KEY_DATASET)) return;
        if (!savedInstanceState.containsKey(KEY_SELECTED_TAB)) return;

        selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB);
        mBinding.tabs.getTabAt(selectedTab).select();

        moviesDataset = savedInstanceState.getParcelable(KEY_DATASET);
        MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(moviesDataset.getResults());
        adapter.notifyDataSetChanged();


        if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
            firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
        else
            firstVisibleItemPosition = 0;
        mBinding.listRecyclerview.smoothScrollToPosition(firstVisibleItemPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (moviesDataset != null)
            outState.putParcelable(KEY_DATASET, moviesDataset);
        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
        outState.putInt(KEY_SELECTED_TAB, selectedTab);
    }

    private void showProgressBar() {
        mBinding.swipeRefresh.setRefreshing(true);
    }

    private void hideProgressBar() {
        mBinding.swipeRefresh.setRefreshing(false);
    }

    private void initTabs() {
        /* Set up the tab layout */
        mBinding.tabs.setVisibility(View.VISIBLE);
        mBinding.tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        mBinding.tabs.setTabMode(TabLayout.MODE_FIXED);

        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(R.string.title_popular_movies_short));
        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(R.string.title_top_rated_movies_short));
        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(R.string.title_favourites));

        mBinding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                switch (tab.getPosition()){
                    case 0:
                        startLoader(LOADER_POPULAR_MOVIES_ID);
                        break;
                    case 1:
                        startLoader(LOADER_TOP_RATED_MOVIES_ID);
                        break;
                    case 2:
                        startLoader(LOADER_FAVOURITE_MOVIES_ID);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initGrid() {
        /*
         * The second parameter is for the number of rows.
         * the third parameter is for the horizontal scroll.
         * the fourth parameter is boolean, when it set to false, layout from start to end
         */
        GridLayoutManager gridHorizontal = new GridLayoutManager(this,
                3,
                GridLayoutManager.VERTICAL,
                false);
        mBinding.listRecyclerview.setLayoutManager(gridHorizontal);
        mBinding.listRecyclerview.setHasFixedSize(true);

        final MoviesGridAdapter moviesAdapter = new MoviesGridAdapter(this);
        mBinding.listRecyclerview.setAdapter(moviesAdapter);

        mBinding.listRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean listScrolling = false;
            boolean shouldUpdate = false;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItems = mBinding.listRecyclerview.getAdapter().getItemCount();
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();
                lastItemPosition = layoutManager.findLastVisibleItemPosition();
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                shouldUpdate = lastItemPosition == totalItems - 1;

                if (!listScrolling && shouldUpdate && selectedTab != TAB_FAVOURITE_MOVIES)
                    loadMoreMovies();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        listScrolling = false;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        listScrolling = true;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        listScrolling = true;
                        break;
                }

                if (!listScrolling && shouldUpdate && selectedTab != TAB_FAVOURITE_MOVIES)
                    loadMoreMovies();
            }
        });
    }

    private void startLoader(int loader_id) {
        mBinding.swipeRefresh.setRefreshing(true);

        if (loader_id == LOADER_FAVOURITE_MOVIES_ID || NetworkUtils.isDeviceOnline(this)) {
            getSupportLoaderManager()
                    .restartLoader(loader_id, null, MainActivity.this)
                    .forceLoad();
        }
        else {
            mBinding.swipeRefresh.setRefreshing(false);

            Snackbar.make(mBinding.coordinatorView, R.string.error_no_internet,
                    Snackbar.LENGTH_LONG).setAction(R.string.retry, v -> {
                getSupportLoaderManager()
                        .restartLoader(loader_id, null, MainActivity.this)
                        .forceLoad();
            }).show();

            moviesDataset = new Dataset<>(0, 1, 0, new ArrayList<>());
            handleMovies();
        }
    }

    synchronized private void loadMoreMovies() {
        if (moviesDataset != null && moviesDataset.getPage() < moviesDataset.getTotalPages()) {
            int loader_id = LOADER_POPULAR_MOVIES_ID;
            switch (selectedTab) {
                case TAB_POPULAR_MOVIES:
                    loader_id = LOADER_MORE_POPULAR_MOVIES_ID;
                    break;
                case TAB_TOP_RATED_MOVIES:
                    loader_id = LOADER_MORE_TOP_RATED_MOVIES_ID;
                    break;
                case TAB_FAVOURITE_MOVIES:
                    loader_id = LOADER_MORE_FAVOURITE_MOVIES_ID;
                    break;
            }

            startLoader(loader_id);
        }
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_DATASET))
            onRefresh();
    }


    @Override
    public void onRefresh() {
        int loader_id = LOADER_POPULAR_MOVIES_ID;
        switch (selectedTab) {
            case TAB_POPULAR_MOVIES:
                loader_id = LOADER_POPULAR_MOVIES_ID;
                break;
            case TAB_TOP_RATED_MOVIES:
                loader_id = LOADER_TOP_RATED_MOVIES_ID;
                break;
            case TAB_FAVOURITE_MOVIES:
                loader_id = LOADER_FAVOURITE_MOVIES_ID;
                break;
        }

        startLoader(loader_id);
    }

    private void handleMovies() {
        if (moviesDataset != null) {
            MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
            adapter.setList(moviesDataset.getResults());
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.KEY_MOVIE, movie);
        startActivity(intent);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_MORE_POPULAR_MOVIES_ID:
                showProgressBar();
                int nextPage = moviesDataset.getPage() + 1;
                return MoviesLoader.createPopularMoviesLoader(this, nextPage);

            case LOADER_POPULAR_MOVIES_ID:
                showProgressBar();
                return MoviesLoader.createPopularMoviesLoader(this, 1);

            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                showProgressBar();
                nextPage = moviesDataset.getPage() + 1;
                return MoviesLoader.createTopRatedMoviesLoader(this, nextPage);

            case LOADER_TOP_RATED_MOVIES_ID:
                showProgressBar();
                return MoviesLoader.createTopRatedMoviesLoader(this, 1);

            case LOADER_FAVOURITE_MOVIES_ID:
            case LOADER_MORE_FAVOURITE_MOVIES_ID:
                showProgressBar();

                Uri forecastQueryUri = MoviesContract.MovieEntry.CONTENT_URI;
                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_MOVIES_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        hideProgressBar();

        switch (loader.getId()) {
            case LOADER_POPULAR_MOVIES_ID:
            case LOADER_TOP_RATED_MOVIES_ID:
                moviesDataset = (Dataset<Movie>) data;
                break;

            case LOADER_MORE_POPULAR_MOVIES_ID:
            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                moviesDataset.setPage(((Dataset<Movie>) data).getPage());
                moviesDataset.getResults().addAll(((Dataset<Movie>) data).getResults());
                break;

            case LOADER_FAVOURITE_MOVIES_ID:
            case LOADER_MORE_FAVOURITE_MOVIES_ID:
                List<Movie> movies = parseCursor((Cursor) data);
                moviesDataset = new Dataset<>(0, movies.size(), 1, movies);
                break;
        }

        handleMovies();
    }

    private List<Movie> parseCursor(Cursor cursor) {
        List<Movie> movies = new ArrayList<>();

        if (cursor != null) for (int i=0; i<cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            int id = cursor.getInt(INDEX_MOVIE_ID);
            float voteAverage = cursor.getFloat(INDEX_MOVIE_VOTE_AVERAGE);
            String posterPath = cursor.getString(INDEX_MOVIE_POSTER_PATH);
            String originalTitle = cursor.getString(INDEX_MOVIE_ORIGINAL_TITLE);
            String overview = cursor.getString(INDEX_MOVIE_OVERVIEW);
            long releaseDate = cursor.getLong(INDEX_MOVIE_RELEASE_DATE);

            Movie movie = new Movie(id, voteAverage, posterPath, originalTitle, overview, releaseDate);
            movies.add(movie);
        }

        return movies;
    }

    @Override
    public void onLoaderReset(Loader loader) { }
}
