package com.michaellundie.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Async Loader extended class. Allows asynchronous loading of our API data download query.
 */
public class NewsAsyncLoader extends AsyncTaskLoader<ArrayList<NewsItem>> {

    private static final String LOG_TAG = NewsAsyncLoader.class.getSimpleName();
    private ArrayList<NewsItem> bookQueryResults = null;
    private String urlString;

    /**
     * Async Loader constructor
     * @param context context of current activity
     * @param url the api request URL constructed using user search queries
     */
    NewsAsyncLoader(Context context, String url) {
        super(context);
        this.urlString = url;
    }

    @Override
    protected void onStartLoading() {
        // Let's check for cached data
        if (bookQueryResults !=null) {
            //Use cached data
            deliverResult(bookQueryResults);
            Log.i(LOG_TAG, "TEST: BookSearchAsyncLoader: Delivering some cached data.");
        } else {
            //There is no data so let's get the party started!
            forceLoad();
            Log.i(LOG_TAG, "TEST: BookSearchAsyncLoader: onStartLoading executed");
        }
    }

    @Override
    public ArrayList<NewsItem> loadInBackground() {
        Log.i(LOG_TAG, "TEST: BookSearchAsyncLoader: loadInBackground executed");

        //Let's check to make sure our URL isn't empty for some reason.
        //We should never have spaces before or after our URL here. Not using trim()
        if (!TextUtils.isEmpty(urlString)) {
            try {
                // Everything is a-okay. Continue to fetch results.
                ArrayList<NewsItem> resultItems = QueryUtils.fetchBookResults(urlString);
                if (resultItems != null) {
                    // Fetch results are not null. Assign to our return variable.
                    bookQueryResults = resultItems;
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                Log.e("Log error", "Problem with Requested URL", e);
            }
        }
        return bookQueryResults;
    }

    @Override
    public void deliverResult(ArrayList<NewsItem> data) {
        super.deliverResult(data);
    }
}