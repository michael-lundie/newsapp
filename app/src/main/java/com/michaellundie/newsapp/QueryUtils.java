package com.michaellundie.newsapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from Google Books API.
 */
public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     */
    private QueryUtils() {
    }

    /**
     * Method for building our query URL
     * @param context The current activity context.
     * @return url string
     */
    public static String queryRequestBuilder (Context context, String returnQuantity, String orderByValue){

        //Set up our variables ready for our string builder
        //NOTE: Is it better to initialise these outside of this method? Are they recreated and
        // destroyed every time the method is called?
        final String API_AUTHORITY = context.getResources().getString(R.string.api_authority);
        final String API_SEARCH_PATH = context.getResources().getString(R.string.api_search_path);
        final String API_SHOWTAGS_PARAM = context.getResources().getString(R.string.api_showTags_param);
        final String API_SHOWFIELDS_PARAM = context.getResources().getString(R.string.api_showFields_param);
        final String API_CONTRIBUTOR_VALUE = context.getResources().getString(R.string.api_showTags_contributor_value);
        final String API_THUMBNAIL_VALUE = context.getResources().getString(R.string.api_showFields_thumbnail_value);
        final String API_QUERY_PARAM = context.getResources().getString(R.string.api_query_param);
        final String API_RESULTS_PARAM = context.getResources().getString(R.string.api_return_param);
        final String API_ORDERBY_PARAM = "order-by";
        final String API_KEY_PARAM = context.getResources().getString(R.string.api_key_param);
        final String API_KEY_VALUE = context.getResources().getString(R.string.api_key_value);

        //Use URL builder to construct our URL
        Uri.Builder query = new Uri.Builder();
        query.scheme("https")
                .authority(API_AUTHORITY)
                .appendPath(API_SEARCH_PATH)
                .appendQueryParameter(API_QUERY_PARAM, "technology")
                .appendQueryParameter(API_ORDERBY_PARAM, orderByValue)
                .appendQueryParameter(API_SHOWTAGS_PARAM, API_CONTRIBUTOR_VALUE)
                .appendQueryParameter(API_SHOWFIELDS_PARAM, API_THUMBNAIL_VALUE)
                .appendQueryParameter(API_RESULTS_PARAM, returnQuantity)
                .appendQueryParameter(API_KEY_PARAM, API_KEY_VALUE)
                .build();
        URL returnUrl = null;

        //Attempt to return our URL, check for exception, then convert to String on return.
        try {
            returnUrl = new URL(query.toString());
        } catch (MalformedURLException e) {
            //We'll do further checking in AsyncLoader, but perhaps it's nice to check for
            //any initial errors.
            Log.e(LOG_TAG, "There is a problem with URL construction.", e);
        }
        //Handle any null pointer exception that may be thrown by .toString() method;
        if (returnUrl == null) {
            Log.i(LOG_TAG, "URL returned null.");
            return null;
        } return returnUrl.toString();
    }

    /**
     * Query the Google Books API and return an {@link List<NewsItem>} object to represent a.
     * list of earthquakes
     * @param requestUrl the URL for our API data request
     * @return parsed JSON query results (as a NewsItem object)
     */
    public static ArrayList<NewsItem> fetchQueryResults(String requestUrl) {
        Log.i(LOG_TAG, "TEST: fetchQueryResults: method called");
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and return a List<NewsItem> object
        return extractNewsResults(jsonResponse);
    }

    /**
     * Checks to make sure the smart phone has access to the internet.
     * @param context the application context
     * @return boolean
     */
    public static boolean checkNetworkAccess(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check the connectivity manager is not null first to avoid NPE.
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            // Returns true or false depending on connectivity status.
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
        //Connectivity manager is null so returning false.
        return false;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Google Books JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link NewsItem} objects that has been built up from
     * parsing a JSON response.
     */
    private static ArrayList<NewsItem> extractNewsResults(String newsQueryJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsQueryJSON)) {

            return null;
        }

        // Create an empty List that we can start adding earthquakes to
        ArrayList<NewsItem> newsQueryResults = new ArrayList<>();

        try {
            // Assign our returned query string to a new JSONObject
            JSONObject jsonObj = new JSONObject(newsQueryJSON);

            // Get results object

            JSONObject resultsJsonO = jsonObj.getJSONObject("response");

            // Get JSONArray items from string
            JSONArray newsItemsJsonA = resultsJsonO.getJSONArray("results");

            //Set up loop to parse each item in our JSONObject
            for (int articleNumber = 0; articleNumber < newsItemsJsonA.length(); articleNumber++) {

                JSONObject currentArticleJsonO = newsItemsJsonA.getJSONObject(articleNumber);

                String section = currentArticleJsonO.optString("sectionName");

                String title = currentArticleJsonO.optString("webTitle");

                String datePublished = currentArticleJsonO.optString("webPublicationDate");

                String articleURL = currentArticleJsonO.optString("webUrl");

                // Okay. We need to go a little deeper down the JSON hierarchy to get our next results.
                // Getting thumbnail URL from guardian API JSON object 'fields'

                JSONObject fieldsJsonO = currentArticleJsonO.getJSONObject("fields");
                String thumbnailURL;
                if(fieldsJsonO != null) {
                    thumbnailURL = fieldsJsonO.optString("thumbnail");
                } else {
                    thumbnailURL = null;
                }

                // Getting authors from guardian API JSON array 'tags'
                JSONArray tagsJsonA = currentArticleJsonO.getJSONArray("tags");
                ArrayList<String> authors = new ArrayList<>();

                if (tagsJsonA != null) {
                    // Add each author to a Java ArrayList object.
                    for (int authorNumber = 0; authorNumber < tagsJsonA.length(); authorNumber++) {
                        JSONObject currentAuthorJsonO = tagsJsonA.getJSONObject(authorNumber);
                        authors.add(currentAuthorJsonO.getString("webTitle"));
                    }
                }

                newsQueryResults.add(new NewsItem(title, authors, section, datePublished, thumbnailURL, articleURL, articleNumber));
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the JSON results.", e);
        }

        // Return the list of news articles
        return newsQueryResults;
    }
}