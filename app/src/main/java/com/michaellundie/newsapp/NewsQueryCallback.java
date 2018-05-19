package com.michaellundie.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Loader callback manager. Manages ongoing process in AsyncLoader and allows us to receive results
 * from our API request straight away once the AsyncLoader thread has completed it's work.
 */
public class NewsQueryCallback implements LoaderManager.LoaderCallbacks<ArrayList<NewsItem>> {

    private static final String LOG_TAG = NewsQueryCallback.class.getSimpleName();
    private Context context;
    private ArrayList<NewsItem> list;
    private String connectURL;
    private ProgressBar progressRing;
    private TextView emptyStateTextView;
    private RecycleViewWithSetEmpty.Adapter adapter;

    private NewsAsyncLoader mLoader;

    NewsQueryCallback(Context context, String connectURL, ArrayList<NewsItem> list,
                      RecycleViewWithSetEmpty.Adapter adapter, ProgressBar bar,
                      TextView emptyStateView) {
        this.context = context;
        this.connectURL = connectURL;
        this.list = list;
        this.adapter = adapter;
        this.progressRing = bar;
        this.emptyStateTextView = emptyStateView;
    }

    @Override
    public Loader<ArrayList<NewsItem>> onCreateLoader(int id, Bundle args) {
        if (mLoader == null) {
            // It's the first time to request a the loader, lets create a new instance.
            return new NewsAsyncLoader(context, connectURL);
        } else {
            // Let's prevent any NPE on configuration change. Return the current instance.
            // (We are using the same instance ID, so we don't want to cause problems here).
            return mLoader;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // Reset was called. Clear our local ArrayList and notify our recyclerview adapter of the
        // change.
        mLoader = null;
        list.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<NewsItem>> loader, ArrayList<NewsItem> data) {
        //Loading is complete. Clear our local array list and notify the adapter of changes.
        list.clear();
        adapter.notifyDataSetChanged();
        //Load all of our fetched and parsed data into our local ArrayList. Notify adapter.
        list.addAll(data);
        adapter.notifyDataSetChanged();
        //Hide our UI progress spinner
        progressRing.setVisibility(View.GONE);
        emptyStateTextView.setText(R.string.query_noresults);
    }
}