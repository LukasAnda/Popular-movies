package com.lukasanda.popularmovies;

/**
 * Created by lukas on 2/16/2018.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lukasanda.popularmovies.adapters.ReviewAdapter;
import com.lukasanda.popularmovies.adapters.TrailerAdapter;
import com.lukasanda.popularmovies.data.Movie;
import com.lukasanda.popularmovies.data.Review;
import com.lukasanda.popularmovies.data.Trailer;
import com.lukasanda.popularmovies.database.MovieContract;
import com.lukasanda.popularmovies.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment implements NetworkUtils.TrailersRequestCallback,
        TrailerAdapter.Callbacks, NetworkUtils.ReviewsRequestCallback, ReviewAdapter.Callbacks,
        NetworkUtils.ErrorCallback {
    
    @SuppressWarnings("unused")
    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    
    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    public static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";
    public static final String EXTRA_FAVOURITE = "EXTRA_FAVOURITE";
    
    private Movie mMovie;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private Menu menu;
    private volatile boolean isFavourite;
    
    @BindView(R.id.trailer_list)
    RecyclerView mRecyclerViewForTrailers;
    @BindView(R.id.review_list)
    RecyclerView mRecyclerViewForReviews;
    
    @BindView(R.id.movie_title)
    TextView mMovieTitleView;
    @BindView(R.id.movie_overview)
    TextView mMovieOverviewView;
    @BindView(R.id.movie_release_date)
    TextView mMovieReleaseDateView;
    @BindView(R.id.movie_user_rating)
    TextView mMovieRatingView;
    @BindView(R.id.movie_poster)
    ImageView mMoviePosterView;
    
    FloatingActionButton fab;
    
    
    public DetailFragment() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(mMovie.getTitle());
        
        ImageView movieBackdrop = activity.findViewById(R.id.movie_backdrop);
        if (movieBackdrop != null) {
            Glide.with(activity)
                    .load(mMovie.getBackdropUrl())
                    .into(movieBackdrop);
        }
    }
    
    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        
        mMovieTitleView.setText(mMovie.getTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate());
        
        Glide.with(getContext())
                .load(mMovie.getPosterUrl())
                .into(mMoviePosterView);
        
        if (mMovie.getUserRating() != null) {
            String userRatingStr = getResources().getString(R.string.user_rating_movie,
                    mMovie.getUserRating());
            mMovieRatingView.setText(userRatingStr);
            
        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
        
        // For horizontal list of trailers
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
        mTrailerAdapter = new TrailerAdapter(new ArrayList<Trailer>(), this);
        mRecyclerViewForTrailers.setAdapter(mTrailerAdapter);
        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);
        
        // For vertical list of reviews
        mReviewAdapter = new ReviewAdapter(new ArrayList<Review>(), this);
        mRecyclerViewForReviews.setAdapter(mReviewAdapter);
        
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_TRAILERS)) {
                List<Trailer> trailers = savedInstanceState.getParcelableArrayList(EXTRA_TRAILERS);
                mTrailerAdapter.add(trailers);
            } else {
                fetchTrailers();
            }
            if (savedInstanceState.containsKey(EXTRA_REVIEWS)) {
                List<Review> reviews = savedInstanceState.getParcelableArrayList(EXTRA_REVIEWS);
                mReviewAdapter.add(reviews);
            } else {
                fetchReviews();
            }
            if (savedInstanceState.containsKey(EXTRA_FAVOURITE)) {
                isFavourite = savedInstanceState.getBoolean(EXTRA_FAVOURITE, false);
            } else {
                isFavourite = false;
            }
            
        } else {
            fetchReviews();
            fetchTrailers();
            isFavourite = false;
        }
        //We can not use ButterKnife because we need to reference views from Activity
        AppBarLayout mAppBarLayout = getActivity().findViewById(R.id.app_bar);
        if (mAppBarLayout != null)
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = false;
                int scrollRange = -1;
                
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        isShow = true;
                        showOption(R.id.action_favourite);
                    } else if (isShow) {
                        isShow = false;
                        hideOption(R.id.action_favourite);
                    }
                }
            });
        
        fab = getActivity().findViewById(R.id.favourite);
        fab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isFavourite)
                            removeFromFavorites();
                        else
                            markAsFavorite();
                    }
                });
        return rootView;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Trailer> trailers = mTrailerAdapter.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_TRAILERS, trailers);
        }
        
        ArrayList<Review> reviews = mReviewAdapter.getReviews();
        if (reviews != null && !reviews.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_REVIEWS, reviews);
        }
        
        outState.putBoolean(EXTRA_FAVOURITE, isFavourite);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.movie_detail, menu);
        hideOption(R.id.action_favourite);
        updateFavoriteButton();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favourite:
                if (isFavourite)
                    removeFromFavorites();
                else
                    markAsFavorite();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void watch(Trailer trailer, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
    }
    
    @Override
    public void read(Review review, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(review.getUrl())));
    }
    
    
    private void fetchTrailers() {
        NetworkUtils.fetchTrailers(mMovie.getId(), this, this);
    }
    
    private void fetchReviews() {
        NetworkUtils.fetchReviews(mMovie.getId(), this, this);
    }
    
    public void markAsFavorite() {
        
        new MarkAsFavouriteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public void removeFromFavorites() {
        new RemoveFromFavouritiesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    
    private void updateFavoriteButton() {
        new AddToFavouritiesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
    }
    
    private boolean isFavorite() {
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);
        
        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }
    
    
    @Override
    public void onTrailersDownloaded(List<Trailer> trailers) {
        mTrailerAdapter.add(trailers);
    }
    
    @Override
    public void onReviewsDownloaded(List<Review> reviews) {
        mReviewAdapter.add(reviews);
    }
    
    @Override
    public void onError() {
        Toast.makeText(getContext(), R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
    }
    
    private void setMenuIcon(int id, boolean filled) {
        //We could do with some kind of animation but it would just complicated the code
        MenuItem item = menu.findItem(id);
        if (filled) {
            fab.setImageResource(R.drawable.ic_heart_filled_24dp);
            item.setIcon(R.drawable.ic_heart_filled_24dp);
        } else {
            fab.setImageResource(R.drawable.ic_heart_blank_24dp);
            item.setIcon(R.drawable.ic_heart_blank_24dp);
        }
    }
    
    private void hideOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }
    
    private void showOption(int id) {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }
    
    private class RemoveFromFavouritiesTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
            if (isFavorite()) {
                getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                        null);
                
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void aVoid) {
            updateFavoriteButton();
        }
    }
    
    private class AddToFavouritiesTask extends AsyncTask<Void, Void, Boolean> {
        
        @Override
        protected Boolean doInBackground(Void... params) {
            return isFavorite();
        }
        
        @Override
        protected void onPostExecute(Boolean isFavorite) {
            isFavourite = isFavorite;
            setMenuIcon(R.id.action_favourite, isFavorite);
        }
    }
    
    private class MarkAsFavouriteTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... params) {
            if (!isFavorite()) {
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                        mMovie.getId());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
                        mMovie.getTitle());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                        mMovie.getPoster());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                        mMovie.getOverview());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                        mMovie.getUserRating());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                        mMovie.getReleaseDate());
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                        mMovie.getBackdrop());
                getContext().getContentResolver().insert(
                        MovieContract.MovieEntry.CONTENT_URI,
                        movieValues
                );
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void aVoid) {
            updateFavoriteButton();
        }
    }
}
