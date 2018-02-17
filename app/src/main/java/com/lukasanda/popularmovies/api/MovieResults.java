package com.lukasanda.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.lukasanda.popularmovies.data.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 2/16/2018.
 */

public class MovieResults {
    
    @SerializedName("results")
    private List<Movie> movies = new ArrayList<>();
    
    public List<Movie> getMovies() {
        return movies;
    }
    
}
