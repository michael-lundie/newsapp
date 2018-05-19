package com.michaellundie.newsapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An extended RecyclerView adapter managing parsed query results and displaying them on the UI.
 */
public class NewsResultsViewAdapter extends RecyclerView.Adapter<NewsResultsViewAdapter.ViewHolder> {

    public static final String LOG_TAG = NewsResultsViewAdapter.class.getSimpleName();
    private Context mContext;
    private final ArrayList<NewsItem> mValues;
    private BitmapDrawable nothumbnail;

    public NewsResultsViewAdapter(ArrayList<NewsItem> items, Context context) {
        mValues = items;
        mContext = context;
        /*nothumbnail = new BitmapDrawable(BitmapFactory.decodeResource
                (mContext.getResources(), R.drawable.no_thumbnail));*/
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull NewsResultsViewAdapter.ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());

        ArrayList<String> authorArray = holder.mItem.getAuthors();
        String author_names = null;

        // Let's handle the authors data. First check if author data was returned.
        if(authorArray.isEmpty()){
            // No data: set string appropriately.
            author_names = holder.mView.getResources().getString(R.string.no_authors);
        } else {
            // Data returned. Begin a string builder and append authors (in case of multiple).
            StringBuilder names_builder = new StringBuilder();
            for (int authorNumber = 0; authorNumber < authorArray.size(); authorNumber++) {
                if (authorNumber == 0) {
                    names_builder.append(authorArray.get(authorNumber));
                } else {
                    names_builder.append(", " + authorArray.get(authorNumber));
                }
                author_names = names_builder.toString();
            }
        }

        // Set text for author names.
        holder.mAuthorView.setText(author_names);

        // Set up our thumbnail imageView object.
        ImageView imageView = (ImageView) holder.mThumbnailView;

        // Fetch the URL we will use for downloading our image
        String dataItem = holder.mItem.getThumbnailURL();

        Log.i(LOG_TAG, "TEST: URL at adapter:" + dataItem);

        // -Begin edited code from https://stackoverflow.com/a/22855962/9738433-
        BitmapDrawable image = CacheManager.getInstance().getBitmapFromMemCache(holder.mItem.getItemID());

        if(image != null) {
            // We have results in our cache for this image. Hide the progress spinner and load the image
            holder.thumbnailProgressBar.setVisibility(View.INVISIBLE);
            holder.mThumbnailView.setVisibility(View.VISIBLE);
            //I removed the custom ImageViews from the original code example
            imageView.setImageDrawable(image);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            //Images are not in cache for this item so begin our Async stream the thumbnail
            // Set up loading spinner on our thumbnail views
            holder.thumbnailProgressBar.setVisibility(View.VISIBLE);
            holder.mThumbnailView.setVisibility(View.INVISIBLE);
            HashMap<Integer, String> imageAndViewPair = new HashMap<Integer, String>();
            imageAndViewPair.put(holder.mThumbnailViewId, holder.mItem.getThumbnailURL());
            loadImagesAsync(imageAndViewPair, holder.mView, holder.mItem.getItemID(), holder.thumbnailProgressBar);
        }
        // -End edit code from https://stackoverflow.com/a/22855962/9738433-
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitleView;
        final TextView mAuthorView;
        final Integer mThumbnailViewId;
        final ImageView mThumbnailView;
        final ProgressBar thumbnailProgressBar;
        NewsItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mAuthorView = (TextView) view.findViewById(R.id.author);
            mThumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            mThumbnailViewId = R.id.thumbnail;
            thumbnailProgressBar = (ProgressBar) view.findViewById(R.id.thumb_progress_spinner);
        }
    }

    // Altered image Async download code from:
    // https://android.jlelse.eu/async-loading-images-on-android-like-a-big-baws-fd97d1a91374
    private void loadImagesAsync(final Map<Integer, String> bindings, final View view, final int id, final ProgressBar progressBar) {
        for (final Map.Entry<Integer, String> binding :
                bindings.entrySet()) {
            new DownloadImageAsync(new DownloadImageAsync.Listener() {
                ImageView thumbnailView = view.findViewById(binding.getKey());

                @Override
                public void onImageDownloaded(final Bitmap bitmap) {
                    // Create a new bitmap drawable
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
                    //Add the drawable to our cache instance
                    CacheManager.getInstance().addBitmapToMemoryCache(id, bitmapDrawable);
                    //Set the drawable to our fragment thumbnail view
                    thumbnailView.setImageDrawable(bitmapDrawable);
                    //Show our thumbnail
                    thumbnailView.setVisibility(View.VISIBLE);
                    //Hide the UI progress spinner
                    progressBar.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onImageDownloadError() {
                    // Let's hide the thumbnail view since no image was returned.
                    CacheManager.getInstance().addBitmapToMemoryCache(id, nothumbnail);
                    thumbnailView.setImageDrawable(nothumbnail);
                    thumbnailView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e(LOG_TAG, "Failed to download image for "
                            + binding.getKey());
                }
            }).execute(binding.getValue());
        }
    }
}