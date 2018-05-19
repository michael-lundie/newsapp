package com.michaellundie.newsapp;

import android.app.Dialog;
import android.app.LoaderManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    public static final String LOG_TAG = MainActivity.class.getName();

    // Create Loader Manager as static, to prevent NPE onDestroy
    // NOTE: I tried to find a better way to handle this - but so far unsuccessful.
    private static LoaderManager.LoaderCallbacks<ArrayList<NewsItem>> newsQueryLoaderCallback;

    private RecycleViewWithSetEmpty mRecyclerView;
    private RecycleViewWithSetEmpty.Adapter mAdapter;
    private ArrayList<NewsItem> mList = new ArrayList<>();
    private static final int BOOKSEARCH_LOADER_ID = 1;
    private static String GUARDIANAPI_REQUEST_URL = "http://content.guardianapis.com/search?show-tags=contributor&show-fields=thumbnail%2C%20author&page-size=10&q=technology&api-key=77f187f4-ede0-4ee7-a390-325df2d79fd3";
    private TextView mEmptyStateTextView;
    private ProgressBar mProgressRing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up our content view
        setContentView(R.layout.activity_main);

        // Set up our custom recycler view
        mRecyclerView = (RecycleViewWithSetEmpty) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(false);

        //Set the empty state view for our custom RecycleViewer
        mEmptyStateTextView = (TextView) findViewById(R.id.list_empty);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        //Set up the progress ring view
        mProgressRing = findViewById(R.id.progressRing);

        /*// Setting up our FAB search button
        FloatingActionButton searchDialogButton = findViewById(R.id.fab_search);
        searchDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogActive = true;
                boolean isConnected = QueryUtils.checkNetworkAccess(view.getContext());
                if (!isConnected) {
                    showToast(getResources().getString(R.string.no_connection));
                } else {
                    //Construct a search dialogue if none exists or if a dialog was open on rotation
                    if (searchDialog == null) {
                        searchDialog = searchDialogue();
                    }
                    //Show search dialogue
                    searchDialog.show();

                }
            }
        });*/



        //Check for a saved instance to handle rotation and resume
        if(savedInstanceState != null)
        {
            mList = savedInstanceState.getParcelableArrayList("mList");
            if (mList != null ) {
                getLoaderManager().initLoader(BOOKSEARCH_LOADER_ID, null,
                        newsQueryLoaderCallback);
            } else {
                mList = new ArrayList<>();
            }
        }
        // Create our new custom recycler adapter
        mAdapter = new NewsResultsViewAdapter(mList, this);

        //Check for screen orientation
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == 1) {
            // If portrait mode use Linear Layout
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAdapter);
        } else {
            // If landscape mode set our grid layout to 2 columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            mRecyclerView.setAdapter(mAdapter);
        }

        executeSearch();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void executeSearch() {

        // Create loader from class, as opposed to implementing the LoaderManager withing MainActivity
        // Used assistance and code from: https://stackoverflow.com/a/20839825
        newsQueryLoaderCallback = new NewsQueryCallback(this, GUARDIANAPI_REQUEST_URL, mList, mAdapter,
                mProgressRing, mEmptyStateTextView);

        boolean isConnected = QueryUtils.checkNetworkAccess(this);
        if (!isConnected) {
            // There is no internet connection. Let's deal with that.
            // We already checked for connection, but just in case the user resumed while the dialog
            // was open, perhaps a double check is good here.
            mProgressRing.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            showToast(getResources().getString(R.string.no_connection));
        } else {
            // Looks like we are good to go.
            mEmptyStateTextView.setVisibility(View.GONE);
            // Let's get our loader manager hooked up and started
            getLoaderManager().initLoader(BOOKSEARCH_LOADER_ID, null, newsQueryLoaderCallback);
        }
    }

    /*
      Helper methods.
     */
    private void showToast(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }
    private void setBackground(View view, int resourceID) {
        view.setBackgroundDrawable(ContextCompat.getDrawable(this, resourceID));
    }
}