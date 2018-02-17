package com.lukasanda.popularmovies.utils;

import android.support.annotation.NonNull;

import com.lukasanda.popularmovies.BuildConfig;
import com.lukasanda.popularmovies.api.ApiClient;
import com.lukasanda.popularmovies.api.ApiInterface;
import com.lukasanda.popularmovies.api.MovieResults;
import com.lukasanda.popularmovies.api.ReviewResults;
import com.lukasanda.popularmovies.api.TrailerResults;
import com.lukasanda.popularmovies.data.Movie;
import com.lukasanda.popularmovies.data.Review;
import com.lukasanda.popularmovies.data.Trailer;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by lukas on 2/16/2018.
 */

public class NetworkUtils {
    public static final int TYPE_POPULAR = 0;
    public static final int TYPE_TOP_RATED = 1;
    public static final int TYPE_FAVOURITES = 2;
    
    public interface MoviesRequestCallback {
        void onMoviesDownloaded(List<Movie> movies);
    }
    
    public interface TrailersRequestCallback {
        void onTrailersDownloaded(List<Trailer> trailers);
    }
    
    public interface ReviewsRequestCallback {
        void onReviewsDownloaded(List<Review> reviews);
    }
    
    public interface ErrorCallback {
        void onError();
    }
    
    public static void fetchMoviesByType(int type,
                                         final MoviesRequestCallback callback,
                                         final ErrorCallback errorCallback) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<MovieResults> fetchMovies;
        
        switch (type) {
            case TYPE_POPULAR:
                fetchMovies = apiInterface.getPopularMovies(BuildConfig.TMDB_API_KEY);
                break;
            case TYPE_TOP_RATED:
                fetchMovies = apiInterface.getTopRatedMovies(BuildConfig.TMDB_API_KEY);
                break;
            default:
                fetchMovies = apiInterface.getPopularMovies(BuildConfig.TMDB_API_KEY);
                break;
        }
        
        fetchMovies.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(@NonNull Call<MovieResults> call,
                                   @NonNull Response<MovieResults> response) {
                if (response.isSuccessful()) {
                    if (callback != null && response.body() != null)
                        callback.onMoviesDownloaded(response.body().getMovies());
                } else {
                    if(errorCallback!=null)errorCallback.onError();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<MovieResults> call, @NonNull Throwable t) {
                if(errorCallback!=null)errorCallback.onError();
            }
        });
    }
    
    public static void fetchTrailers(long id,
                                         final TrailersRequestCallback callback,
                                         final ErrorCallback errorCallback) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<TrailerResults> fetchTrailers = apiInterface.getTrailers(id, BuildConfig.TMDB_API_KEY);
        
        fetchTrailers.enqueue(new Callback<TrailerResults>() {
            @Override
            public void onResponse(@NonNull Call<TrailerResults> call,
                                   @NonNull Response<TrailerResults> response) {
                if(response.isSuccessful()){
                    if(callback!=null && response.body()!=null){
                        callback.onTrailersDownloaded(response.body().getTrailers());
                    }
                } else {
                    if(errorCallback!= null) errorCallback.onError();
                }
            }
    
            @Override
            public void onFailure(@NonNull Call<TrailerResults> call, @NonNull Throwable t) {
                if(errorCallback!= null) errorCallback.onError();
            }
        });
    }
    
    public static void fetchReviews(long id,
                                     final ReviewsRequestCallback callback,
                                     final ErrorCallback errorCallback) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ReviewResults> fetchReviews = apiInterface.getReviews(id, BuildConfig.TMDB_API_KEY);
        
        fetchReviews.enqueue(new Callback<ReviewResults>() {
            @Override
            public void onResponse(@NonNull Call<ReviewResults> call,
                                   @NonNull Response<ReviewResults> response) {
                if(response.isSuccessful()){
                    if(callback!=null && response.body()!=null){
                        callback.onReviewsDownloaded(response.body().getReviews());
                    }
                } else {
                    if(errorCallback!= null) errorCallback.onError();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ReviewResults> call, @NonNull Throwable t) {
                if(errorCallback!= null) errorCallback.onError();
            }
        });
    }
}
