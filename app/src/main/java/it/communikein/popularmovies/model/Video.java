package it.communikein.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Video extends ParcelableItem {

    public static final String ID = "id";
    public static final String KEY = "key";
    public static final String NAME = "name";
    public static final String WEBSITE = "website";
    public static final String MOVIE_ID = "movie_id";


    @SerializedName(ID)
    private String id;

    @SerializedName(KEY)
    private String key;

    @SerializedName(NAME)
    private String name;

    @SerializedName(WEBSITE)
    private String website;

    @SerializedName(MOVIE_ID)
    private int movieId;


    public Video(Parcel in) {
        setId(in.readString());
        setKey(in.readString());
        setName(in.readString());
        setWebsite(in.readString());
        setMovieId(in.readInt());
    }

    public Video(String id, String key, String name, String website, int movieId) {
        setId(id);
        setKey(key);
        setName(name);
        setWebsite(website);
        setMovieId(movieId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }



    public boolean isYoutube() {
        return getWebsite().toLowerCase().equals("youtube");
    }



    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Video)) return false;

        Video other = (Video) obj;
        return this.getId().equals(other.getId());
    }

    public boolean displayEquals(Object obj) {
        if (! (obj instanceof Video)) return false;

        Video other = (Video) obj;
        return this.getKey().equals(other.getKey()) &&
                this.getName().equals(other.getName()) &&
                this.getWebsite().equals(other.getWebsite());
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getKey());
        dest.writeString(getName());
        dest.writeString(getWebsite());
        dest.writeInt(getMovieId());
    }

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {

        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
