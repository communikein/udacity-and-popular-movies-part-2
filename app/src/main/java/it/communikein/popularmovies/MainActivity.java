package it.communikein.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import it.communikein.popularmovies.databinding.ActivityMainBinding;
import it.communikein.popularmovies.network.MoviesLoader;


public class MainActivity extends AppCompatActivity implements
        MoviesGridAdapter.MovieClickCallback, LoaderManager.LoaderCallbacks {

    private static final String KEY_DATASET = "DATASET";
    private static final String KEY_FIRST_VISIBLE_ITEM_POS = "FIRST_VISIBLE_ITEM_POS";
    private static final String KEY_POPULAR = "POPULAR";

    private static final int LOADER_POPULAR_MOVIES_ID = 1001;
    private static final int LOADER_TOP_RATED_MOVIES_ID = 1002;

    private static final int LOADER_MORE_POPULAR_MOVIES_ID = 1011;
    private static final int LOADER_MORE_TOP_RATED_MOVIES_ID = 1012;

    private ActivityMainBinding mBinding;

    private DatasetMovies datasetMovies;
    private boolean popular = true;
    private int lastItemPosition = -1;
    private int firstVisibleItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mBinding.toolbar);

        hideProgressBar();
        initGrid();
        initData(savedInstanceState);
        initFab();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hideProgressBar();
        if (savedInstanceState == null) return;
        if (!savedInstanceState.containsKey(KEY_DATASET)) return;
        if (!savedInstanceState.containsKey(KEY_POPULAR)) return;

        popular = savedInstanceState.getBoolean(KEY_POPULAR);
        updateMovies();

        datasetMovies = savedInstanceState.getParcelable(KEY_DATASET);

        if (savedInstanceState.containsKey(KEY_FIRST_VISIBLE_ITEM_POS))
            firstVisibleItemPosition = savedInstanceState.getInt(KEY_FIRST_VISIBLE_ITEM_POS);
        else
            firstVisibleItemPosition = 0;

        MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
        adapter.setList(datasetMovies.getResults());
        adapter.notifyDataSetChanged();

        mBinding.listRecyclerview.scrollToPosition(firstVisibleItemPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_DATASET, datasetMovies);
        outState.putInt(KEY_FIRST_VISIBLE_ITEM_POS, firstVisibleItemPosition);
        outState.putBoolean(KEY_POPULAR, popular);
    }

    private void showProgressBar() {
        mBinding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mBinding.progressBar.setVisibility(View.GONE);
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

                if (!listScrolling && shouldUpdate)
                    updateMovies();
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

                if (!listScrolling && shouldUpdate)
                    updateMovies();
            }
        });
    }

    synchronized private void updateMovies() {
        loadMoreMovies();
    }

    private void loadMoreMovies() {
        if (datasetMovies != null && datasetMovies.getPage() < datasetMovies.getTotalPages()) {
            int loader_id = popular ? LOADER_MORE_POPULAR_MOVIES_ID : LOADER_MORE_TOP_RATED_MOVIES_ID;

            getSupportLoaderManager()
                    .restartLoader(loader_id, null, this)
                    .forceLoad();
        }
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY_DATASET))
            getSupportLoaderManager()
                    .restartLoader(LOADER_POPULAR_MOVIES_ID, null, this)
                    .forceLoad();
    }

    private void initFab() {
        mBinding.fab.setBackgroundResource(R.drawable.ic_star_white);
        mBinding.fab.setOnClickListener(v -> {
            popular = !popular;
            updateMovieSort();

            int loader_id = popular ? LOADER_POPULAR_MOVIES_ID : LOADER_TOP_RATED_MOVIES_ID;
            getSupportLoaderManager()
                    .restartLoader(loader_id, null, this)
                    .forceLoad();
        });
    }

    private void updateMovieSort() {
        if (popular) {
            mBinding.toolbar.setTitle(R.string.title_popular_movies);
            mBinding.fab.setImageResource(R.drawable.ic_star_white);
        } else {
            mBinding.toolbar.setTitle(R.string.title_top_rated_movies);
            mBinding.fab.setImageResource(R.drawable.ic_whatshot_white);
        }
    }

    private void handleMovies() {
        if (datasetMovies != null) {
            MoviesGridAdapter adapter = (MoviesGridAdapter) mBinding.listRecyclerview.getAdapter();
            adapter.setList(datasetMovies.getResults());
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onListNewsClick(Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.KEY_MOVIE, movie);
        startActivity(intent);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_MORE_POPULAR_MOVIES_ID:
                showProgressBar();
                int nextPage = datasetMovies.getPage() + 1;
                return MoviesLoader.createPopularMoviesLoader(this, nextPage);

            case LOADER_POPULAR_MOVIES_ID:
                showProgressBar();
                return MoviesLoader.createPopularMoviesLoader(this, 1);

            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                showProgressBar();
                nextPage = datasetMovies.getPage() + 1;
                return MoviesLoader.createTopRatedMoviesLoader(this, nextPage);

            case LOADER_TOP_RATED_MOVIES_ID:
                showProgressBar();
                return MoviesLoader.createTopRatedMoviesLoader(this, 1);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        hideProgressBar();

        DatasetMovies newDataset = (DatasetMovies) data;
        switch (loader.getId()) {
            case LOADER_POPULAR_MOVIES_ID:
            case LOADER_TOP_RATED_MOVIES_ID:
                datasetMovies = newDataset;
                break;

            case LOADER_MORE_POPULAR_MOVIES_ID:
            case LOADER_MORE_TOP_RATED_MOVIES_ID:
                datasetMovies.setPage(newDataset.getPage());
                datasetMovies.getResults().addAll(((DatasetMovies) data).getResults());
                break;
        }

        handleMovies();
    }

    @Override
    public void onLoaderReset(Loader loader) { }
}
