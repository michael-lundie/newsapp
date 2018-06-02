package com.michaellundie.newsapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/*
 * A simple class to store and handle News Result data.
 * Implements parcelable to allow for data restoration after a screen rotation.
 */
public class NewsItem implements Parcelable {
    private String title;
    private ArrayList<String> authors;
    private String section;
    private String datePublished;
    private String thumbnailURL;
    private String articleURL;
    private int itemID;

    /**
     * Default object constructor for this class.
     * @param title The title of our news article. String.
     * @param authors An ArrayList of article authors.
     * @param section The section name of the returned article.
     * @param datePublished The published date of the returned article.
     * @param thumbnailURL The URL of any linked image.
     * @param articleURL The URL of the web version of this news article.
     * @param itemID The unique itemID for this item.
     */
    NewsItem(String title, ArrayList<String> authors, String section, String datePublished, String thumbnailURL, String articleURL,
             int itemID) {
        this.title = title;
        this.authors = authors;
        this.section = section;
        this. datePublished = datePublished;
        this.thumbnailURL = thumbnailURL;
        this.articleURL = articleURL;
        this.itemID = itemID;
    }

    /**
     * Constructor taking parcelable (from returned bundle on instanceSaved) as an argument.
     * @param in Parcel object data which has been Marshaled
     */
    private NewsItem(Parcel in) {
        this.title = in.readString();
        this.authors = in.readArrayList(NewsItem.class.getClassLoader());
        this.section = in.readString();
        this. datePublished = in.readString();
        this.thumbnailURL = in.readString();
        this.articleURL = in.readString();
        this.itemID = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeList(authors);
        out.writeString(section);
        out.writeString(datePublished);
        out.writeString(thumbnailURL);
        out.writeString(articleURL);
        out.writeInt(itemID);
    }

    public static final Parcelable.Creator<NewsItem> CREATOR = new Parcelable.Creator<NewsItem>() {
        public NewsItem createFromParcel(Parcel in) {
            return new NewsItem(in);
        }

        public NewsItem[] newArray(int size) {
            return new NewsItem[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public String getTitle() { return title; }

    public ArrayList<String> getAuthors() { return authors; }

    public String getSection() { return section; }

    public String getDatePublished() { return datePublished; }

    public String getThumbnailURL() { return thumbnailURL; }

    public String getArticleURL() { return articleURL; }

    public int getItemID() { return itemID; }
}
