package com.lukasanda.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.lukasanda.popularmovies.data.Review;
import com.lukasanda.popularmovies.data.Trailer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 2/16/2018.
 */

public class TrailerResults {
    
    @SerializedName("results")
    private List<Trailer> trailers = new ArrayList<>();
    
    public List<Trailer> getTrailers() {
        return trailers;
    }
}
