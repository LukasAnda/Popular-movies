package com.lukasanda.popularmovies.api;

import com.google.gson.annotations.SerializedName;
import com.lukasanda.popularmovies.data.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 2/16/2018.
 */

public class ReviewResults {
    
    @SerializedName("results")
    private List<Review> reviews = new ArrayList<>();
    
    public List<Review> getReviews() {
        return reviews;
    }
}
