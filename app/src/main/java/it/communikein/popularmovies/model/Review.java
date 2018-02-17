package it.communikein.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Review extends ParcelableItem {

    public static final String ID = "id";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    public static final String URL = "url";
    public static final String MOVIE_ID = "movie_id";


    @SerializedName(ID)
    private String id;

    @SerializedName(AUTHOR)
    private String author;

    @SerializedName(CONTENT)
    private String content;

    @SerializedName(URL)
    private String url;

    @SerializedName(MOVIE_ID)
    private int movieId;

    private Review(Parcel in) {
        setId(in.readString());
        setAuthor(in.readString());
        setContent(in.readString());
        setUrl(in.readString());
        setMovieId(in.readInt());
    }

    public Review(@NonNull final String id, String author, String content, String url, final int movieId) {
        setId(id);
        setAuthor(author);
        setContent(content);
        setUrl(url);
        setMovieId(movieId);
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Review)) return false;

        Review other = (Review) obj;
        return this.getId().equals(other.getId());
    }

    public boolean displayEquals(Object obj) {
        if (! (obj instanceof Review)) return false;

        Review other = (Review) obj;
        return this.getAuthor().equals(other.getAuthor()) &&
                this.getContent().equals(other.getContent());
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getAuthor());
        dest.writeString(getContent());
        dest.writeString(getUrl());
        dest.writeInt(getMovieId());
    }

    static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {

        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
