package com.lukasanda.popularmovies.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by lukas on 2/16/2018.
 */

public interface ApiInterface {
    
    @GET("movie/popular")
    Call<MovieResults> getPopularMovies(@Query("api_key") String apiKey);
    
    @GET("movie/top_rated")
    Call<MovieResults> getTopRatedMovies(@Query("api_key") String apiKey);
    
    @GET("movie/{id}/videos")
    Call<TrailerResults> getTrailers(@Path("id") long movieId, @Query("api_key") String apiKey);
    
    @GET("movie/{id}/reviews")
    Call<ReviewResults> getReviews(@Path("id") long movieId, @Query("api_key") String apiKey);
}
