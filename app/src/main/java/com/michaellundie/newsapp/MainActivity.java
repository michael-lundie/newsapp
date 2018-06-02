package com.michaellundie.newsapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Displays a list of news results, generated using the guardian news API.
 */
public class MainActivity extends AppCompatActivity  {

    public static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Create Loader Manager as static, to prevent NPE onDestroy
     * NOTE: I tried to find a better way to handle this - but so far unsuccessful.
     */
    private static LoaderManager.LoaderCallbacks<ArrayList<NewsItem>> newsQueryLoaderCallback;

    RecycleViewWithSetEmpty mRecyclerView;
    private RecycleViewWithSetEmpty.Adapter mAdapter;
    private ArrayList<NewsItem> mList = new ArrayList<>();
    private static final int API_REQUEST_LOADER_ID = 1;
    private TextView mEmptyStateTextView;
    private ProgressBar mProgressRing;
    static boolean settingsChanged = false;

    /** A boolean value representing indicating if this is the first time the app has been loaded. */
    static boolean firstLoad = true;

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


        //Check for a saved instance to handle rotation and resume
        if(savedInstanceState != null)
        {
            mList = savedInstanceState.getParcelableArrayList("mList");
            if (mList != null ) {
                getLoaderManager().initLoader(API_REQUEST_LOADER_ID, null,
                        newsQueryLoaderCallback);
            } else {
                mList = new ArrayList<>();
            }
        }

        // Get our default padding size and convert to pixels for the current device.
        // This is used for our spannable string in our ViewAdapter
        Resources resources = getResources();
        // Receives float, but cast to int.
        int paddingInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                resources.getDimension(R.dimen.text_padding), resources.getDisplayMetrics());

        // Initiate our new custom recycler adapter
        mAdapter = new NewsResultsViewAdapter(mList, this, paddingInPixels);

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

        // Setting up our FAB refresh button
        FloatingActionButton searchDialogButton = findViewById(R.id.fab_refresh);
        searchDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isConnected = QueryUtils.checkNetworkAccess(view.getContext());
                if (firstLoad) {
                    firstLoad = false;
                    executeSearch();
                } else {
                    resetSearch();
                    executeSearch();
                }
            }
        });

        // Check to see if it is the first time loading our app.
        if (firstLoad) {
            mProgressRing.setVisibility(View.VISIBLE);
            executeSearch();
            firstLoad = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Override method allowing the passing of intent data when resuming our MainActivity,
     * after accessing the SettingsActivity.
     * @param requestCode Where is the request coming from?
     * @param resultCode Was a result available?
     * @param data Intent data includes a boolean value, representing the change status of settings.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                settingsChanged = data.getBooleanExtra("settingsChanged", false);
                if (settingsChanged) {
                    // Reset out settingsChanged variable
                    settingsChanged = false;
                    // reset search, destroying previous loader and cache
                    resetSearch();
                    // Begin our query using AsyncLoader
                    executeSearch();
                }
            }
        }
    }

    /**
     * Allows seamless transition of our app between the various activity states.
     * @param outState Contains the listArray data (if any) used to populate our RecycleViewer
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Saving parcelable code adapted from : https://stackoverflow.com/a/12503875/9738433
        if (!mList.isEmpty()){
            outState.putParcelableArrayList("mList", mList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent,1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A method containing the necessary logic for executing a reliable API query, checking
     * shared user preferences, if internet access is available and passing our query to the
     * loader manager.
     */
    public void executeSearch() {

        //Get user preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String returnQuantity = sharedPrefs.getString(
                getString(R.string.settings_return_quantity_key),
                getString(R.string.settings_return_quantity_default));
        String returnOrder = sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_newest));

        // Build our Query URL
        String queryURL = QueryUtils.queryRequestBuilder(this, returnQuantity, returnOrder);

        // Create loader from class, as opposed to implementing the LoaderManager withing MainActivity
        // Used assistance and code from: https://stackoverflow.com/a/20839825
        newsQueryLoaderCallback = new NewsQueryCallback(this, queryURL, mList, mAdapter,
                mProgressRing, mEmptyStateTextView);

        boolean isConnected = QueryUtils.checkNetworkAccess(this);
        if (!isConnected) {
            // There is no internet connection. Let's deal with that.
            // We already checked for connection, but just in case the user resumed while the dialog
            // was open, perhaps a double check is good here.
            mProgressRing.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getResources().getString(R.string.no_connection));
            mEmptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            // Looks like we are good to go.
            mEmptyStateTextView.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getResources().getString(R.string.query_noresults));
            // Let's get our loader manager hooked up and started
            getLoaderManager().initLoader(API_REQUEST_LOADER_ID, null, newsQueryLoaderCallback);
        }
    }

    /**
     * A method which resets our loader and clears cache to prepare the adapter for a new query.
     */
    private void resetSearch() {
        // upon a new search initiation, destroy previous loader.
        getLoaderManager().destroyLoader(API_REQUEST_LOADER_ID);
        //clear the array list
        mList.clear();
        //clear our cache
        CacheManager.getInstance().clear();
        //notify the adapter and scroll to position 0
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        // Show our progress ring.
        mProgressRing.setVisibility(View.VISIBLE);
    }
}