package com.lukasanda.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lukasanda.popularmovies.R;
import com.lukasanda.popularmovies.data.Movie;
import com.lukasanda.popularmovies.database.MovieContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lukas on 2/16/2018.
 */

public class MovieAdapter
        extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    
    @SuppressWarnings("unused")
    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();
    
    private final ArrayList<Movie> mMovies;
    private final Callbacks mCallbacks;
    private Context context;
    
    public interface Callbacks {
        void open(Movie movie, int position);
    }
    
    public MovieAdapter(Context context, ArrayList<Movie> movies, Callbacks callbacks) {
        this.context = context;
        mMovies = movies;
        this.mCallbacks = callbacks;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_item, parent, false);
        
        //Some magical formula that keeps the images in required ratio
        view.getLayoutParams().height = (int) (parent.getWidth() / 2 * 1.5);
        
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Movie movie = mMovies.get(position);
        //final Context context = holder.mView.getContext();
        Glide.with(context)
                .load(movie.getPosterUrl())
                .into(holder.mThumbnailView);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return mMovies.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.thumbnail)
        ImageView mThumbnailView;
        
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }
        
    }
    
    public void set(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }
    
    public void set(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(MovieContract.MovieEntry.COL_MOVIE_ID);
                String title = cursor.getString(MovieContract.MovieEntry.COL_MOVIE_TITLE);
                String posterPath = cursor.getString(MovieContract.MovieEntry
                        .COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(MovieContract.MovieEntry.COL_MOVIE_OVERVIEW);
                Double rating = cursor.getDouble(MovieContract.MovieEntry.COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(MovieContract.MovieEntry
                        .COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(MovieContract.MovieEntry
                        .COL_MOVIE_BACKDROP_PATH);
                Movie movie = new Movie(id, title, posterPath, overview, rating, releaseDate,
                        backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }
    
    public ArrayList<Movie> getMovies() {
        return mMovies;
    }
}
