package it.communikein.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Movie extends ParcelableItem {

    public static final String ID = "id";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String POSTER_PATH = "poster_path";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String OVERVIEW = "overview";
    public static final String RELEASE_DATE = "release_date";
    public static final String FAVOURITE = "favourite";
    public static final String REVIEWS = "reviews";
    public static final String VIDEOS = "videos";

    private static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final DecimalFormat voteFormat = new DecimalFormat("#.##");

    @SerializedName(ID)
    private int id;

    @SerializedName(VOTE_AVERAGE)
    private float voteAverage;

    @SerializedName(POSTER_PATH)
    private String posterPath;

    @SerializedName(ORIGINAL_TITLE)
    private String originalTitle;

    @SerializedName(OVERVIEW)
    private String overview;

    @SerializedName(RELEASE_DATE)
    private Date releaseDate;

    @SerializedName(FAVOURITE)
    private boolean favourite;

    @SerializedName(REVIEWS)
    private List<Review> reviews;

    @SerializedName(VIDEOS)
    private List<Video> videos;


    public Movie(Parcel in) {
        setId(in.readInt());
        setVoteAverage(in.readFloat());
        setPosterPath(in.readString());
        setOriginalTitle(in.readString());
        setOverview(in.readString());
        setReleaseDate(in.readString());
        setFavourite(in.readInt() == 1);

        setReviews(new ArrayList<>());
        in.readTypedList(this.reviews, Review.CREATOR);

        setVideos(new ArrayList<>());
        in.readTypedList(this.videos, Video.CREATOR);
    }

    public Movie(final int id, float voteAverage, String posterPath, String originalTitle,
                 String overview, String releaseDate, boolean favourite,
                 List<Review> reviews, List<Video> videos) {
        setId(id);
        setVoteAverage(voteAverage);
        setPosterPath(posterPath);
        setOriginalTitle(originalTitle);
        setOverview(overview);
        setReleaseDate(releaseDate);
        setFavourite(favourite);
        setReviews(reviews);
        setVideos(videos);
    }

    public Movie(final int id, float voteAverage, String posterPath, String originalTitle,
                 String overview, long releaseDate) {
        setId(id);
        setVoteAverage(voteAverage);
        setPosterPath(posterPath);
        setOriginalTitle(originalTitle);
        setOverview(overview);
        setReleaseDate(new Date(releaseDate));
        setFavourite(true);
        setReviews(new ArrayList<>());
        setVideos(new ArrayList<>());
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public String printVoteAverage() {
        return voteFormat.format(getVoteAverage());
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterFullPath() {
        return "http://image.tmdb.org/t/p/w185" + posterPath;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String printReleaseDate() {
        if (getReleaseDate() != null)
            return dateFormat.format(getReleaseDate());
        else
            return "";
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        try {
            this.releaseDate = dateFormat.parse(releaseDate);
        } catch (ParseException e) {
            this.releaseDate = null;
        }
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }



    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Movie)) return false;

        Movie other = (Movie) obj;
        return this.getId() == other.getId();
    }

    public boolean displayEquals(Object obj) {
        if (! (obj instanceof Movie)) return false;

        Movie other = (Movie) obj;
        return this.getPosterPath().equals(other.getPosterPath()) &&
                this.getOriginalTitle().equals(other.getOriginalTitle()) &&
                this.getReleaseDate().getTime() == other.getReleaseDate().getTime();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeFloat(getVoteAverage());
        dest.writeString(getPosterPath());
        dest.writeString(getOriginalTitle());
        dest.writeString(getOverview());
        dest.writeString(printReleaseDate());
        dest.writeInt(isFavourite() ? 1 : 0);
        dest.writeTypedList(getReviews());
        dest.writeTypedList(getVideos());
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
