package com.lukasanda.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.lukasanda.popularmovies.adapters.MovieAdapter;
import com.lukasanda.popularmovies.data.Movie;
import com.lukasanda.popularmovies.database.MovieContract;
import com.lukasanda.popularmovies.utils.NetworkUtils;
import com.lukasanda.popularmovies.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.Callbacks,
        LoaderManager.LoaderCallbacks<Cursor>, NetworkUtils.ErrorCallback, NetworkUtils
                .MoviesRequestCallback {
    
    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";
    private static final int FAVORITE_MOVIES_LOADER = 0;
    private MovieAdapter mAdapter;
    private int sortOrder;
    private SharedPreferences prefs;
    
    @BindView(R.id.movie_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        ButterKnife.bind(this);
        
        mToolbar.setTitle(R.string.title_movie_list);
        setSupportActionBar(mToolbar);
        
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.calculateNoOfColumns(this)));
        mAdapter = new MovieAdapter(this, new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //savedInstancestate has always bigger importance than shared prefs
        if (savedInstanceState != null) {
            sortOrder = savedInstanceState.getInt(EXTRA_SORT_BY, NetworkUtils.TYPE_POPULAR);
            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                List<Movie> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.set(movies);
                findViewById(R.id.progress).setVisibility(View.GONE);
                
                // For listening content updates for tow pane mode
                if (sortOrder == NetworkUtils.TYPE_FAVOURITES) {
                    getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
                }
            }
            if (savedInstanceState.containsKey(EXTRA_POSITION)) {
                mRecyclerView.scrollToPosition(savedInstanceState.getInt(EXTRA_POSITION,0));
            }
            updateUI();
        } else {
            // Fetch Movies only if savedInstanceState == null
            sortOrder = prefs.getInt(EXTRA_SORT_BY, NetworkUtils.TYPE_POPULAR);
            fetchMovies(sortOrder);
        }
        
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Movie> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_MOVIES, movies);
        }
        outState.putInt(EXTRA_SORT_BY, sortOrder);
        int position = ((GridLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        outState.putInt(EXTRA_POSITION, position);
        // we destroy it just in case
        if (sortOrder != NetworkUtils.TYPE_FAVOURITES) {
            getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_activity, menu);
        
        switch (sortOrder) {
            case NetworkUtils.TYPE_POPULAR:
                menu.findItem(R.id.sort_by_most_popular).setChecked(true);
                break;
            case NetworkUtils.TYPE_TOP_RATED:
                menu.findItem(R.id.sort_by_top_rated).setChecked(true);
                break;
            case NetworkUtils.TYPE_FAVOURITES:
                menu.findItem(R.id.sort_by_favorites).setChecked(true);
                break;
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_top_rated:
                if (sortOrder == NetworkUtils.TYPE_FAVOURITES) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                sortOrder = NetworkUtils.TYPE_TOP_RATED;
                fetchMovies(sortOrder);
                item.setChecked(true);
                break;
            case R.id.sort_by_most_popular:
                if (sortOrder == NetworkUtils.TYPE_FAVOURITES) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                sortOrder = NetworkUtils.TYPE_POPULAR;
                fetchMovies(sortOrder);
                item.setChecked(true);
                break;
            case R.id.sort_by_favorites:
                sortOrder = NetworkUtils.TYPE_FAVOURITES;
                fetchMovies(sortOrder);
                item.setChecked(true);
            default:
                break;
        }
        if(prefs!=null)
            prefs.edit().putInt(EXTRA_SORT_BY, sortOrder).apply();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void open(Movie movie, int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailFragment.ARG_MOVIE, movie);
        startActivity(intent);
    }
    
    
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.set(cursor);
        updateUI();
        findViewById(R.id.progress).setVisibility(View.GONE);
        
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Not used
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        return new CursorLoader(this,
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.MOVIE_COLUMNS,
                null,
                null,
                "movie_id ASC");
    }
    
    private void fetchMovies(int sortBy) {
        if (sortBy != NetworkUtils.TYPE_FAVOURITES) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            NetworkUtils.fetchMoviesByType(sortBy, this, this);
        } else {
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }
    
    private void updateUI() {
        if (mAdapter.getItemCount() == 0) {
            if (sortOrder == NetworkUtils.TYPE_FAVOURITES) {
                findViewById(R.id.empty_state_container).setVisibility(View.GONE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.empty_state_container).setVisibility(View.VISIBLE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onError() {
        updateUI();
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
    
    @Override
    public void onMoviesDownloaded(List<Movie> movies) {
        mAdapter.set(movies);
        updateUI();
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
