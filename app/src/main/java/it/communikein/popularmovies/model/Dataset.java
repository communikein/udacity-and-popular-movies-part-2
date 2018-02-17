package it.communikein.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Dataset<T extends ParcelableItem> implements Parcelable {

    public static final String PAGE = "page";
    public static final String TOTAL_RESULTS = "total_results";
    public static final String TOTAL_PAGES = "total_pages";
    public static final String RESULTS = "results";

    @SerializedName(PAGE)
    private int page;

    @SerializedName(TOTAL_RESULTS)
    private int totalResults;

    @SerializedName(TOTAL_PAGES)
    private int totalPages;

    @SerializedName(RESULTS)
    private List<T> results;

    Dataset(Parcel in) {
        setPage(in.readInt());
        setTotalResults(in.readInt());
        setTotalPages(in.readInt());
        setResults(new ArrayList<>());
        in.readTypedList(this.results, T.CREATOR);
    }

    public Dataset(int page, int totalResults, int totalPages, List<T> results) {
        setPage(page);
        setTotalResults(totalResults);
        setTotalPages(totalPages);
        setResults(results);
    }


    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getPage());
        dest.writeInt(getTotalResults());
        dest.writeInt(getTotalPages());
        dest.writeTypedList(getResults());
    }

    static final Parcelable.Creator<Dataset> CREATOR = new Parcelable.Creator<Dataset>() {

        public Dataset createFromParcel(Parcel in) {
            return new Dataset<>(in);
        }

        public Dataset[] newArray(int size) {
            return new Dataset[size];
        }
    };

}
