package com.lukasanda.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.lukasanda.popularmovies.utils.Constants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by lukas on 2/16/2018.
 */

public class Movie implements Parcelable {
    
    @SerializedName("id")
    private long mId;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("poster_path")
    private String mPoster;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("vote_average")
    private Double mUserRating;
    @SerializedName("release_date")
    private String mReleaseDate;
    @SerializedName("backdrop_path")
    private String mBackdrop;
    
    private Movie() {
    }
    
    public Movie(long id, String title, String poster, String overview, Double userRating,
                 String releaseDate, String backdrop) {
        mId = id;
        mTitle = title;
        mPoster = poster;
        mOverview = overview;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
        mBackdrop = backdrop;
    }
    
    @Nullable
    public String getTitle() {
        return mTitle;
    }
    
    public long getId() {
        return mId;
    }
    
    @Nullable
    public String getPosterUrl() {
        if (!TextUtils.isEmpty(mPoster)) {
            return Constants.IMAGE_DOWNLOAD_URL + mPoster;
        }
        return null;
    }
    
    public String getPoster() {
        return mPoster;
    }
    
    public String getReleaseDate() {
        return DateTime.parse(mReleaseDate,
                DateTimeFormat.forPattern("yyyy-MM-dd")).toString(DateTimeFormat.forPattern("dd.MM.yyyy"));
    }
    
    @Nullable
    public String getOverview() {
        return mOverview;
    }
    
    @Nullable
    public Double getUserRating() {
        return mUserRating;
    }
    
    @Nullable
    public String getBackdropUrl() {
        if (!TextUtils.isEmpty(mBackdrop)) {
            return Constants.BACKDROP_DOWNLOAD_URL +
                    mBackdrop;
        }
        return null;
    }
    
    public String getBackdrop() {
        return mBackdrop;
    }
    
    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            Movie movie = new Movie();
            movie.mId = source.readLong();
            movie.mTitle = source.readString();
            movie.mPoster = source.readString();
            movie.mOverview = source.readString();
            movie.mUserRating = source.readDouble();
            movie.mReleaseDate = source.readString();
            movie.mBackdrop = source.readString();
            return movie;
        }
        
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    
    public int describeContents() {
        return 0;
    }
    
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mPoster);
        parcel.writeString(mOverview);
        parcel.writeDouble(mUserRating);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mBackdrop);
    }
}