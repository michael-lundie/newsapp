package com.michaellundie.newsapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An extended RecyclerView adapter managing parsed query results and displaying them on the UI.
 */
public class NewsResultsViewAdapter extends RecyclerView.Adapter<NewsResultsViewAdapter.ViewHolder> {

    public static final String LOG_TAG = NewsResultsViewAdapter.class.getSimpleName();
    private Context mContext;
    private final ArrayList<NewsItem> mValues;
    private final int mPadding;
    private BitmapDrawable nothumbnail;



    public NewsResultsViewAdapter(ArrayList<NewsItem> items, Context context, int padding) {
        mValues = items;
        mContext = context;
        mPadding = padding;
        nothumbnail = new BitmapDrawable(BitmapFactory.decodeResource
                (mContext.getResources(), R.drawable.no_thumbnail));
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

        SpannableString titleSpan = new SpannableString(mValues.get(position).getTitle());

        holder.mTitleView.setShadowLayer(mPadding /* radius */, 0, 0, 0 /* transparent */);
        holder.mTitleView.setPadding(mPadding, mPadding, mPadding, mPadding);
        titleSpan.setSpan(
                new PaddingBackgroundColorSpan(ContextCompat.getColor(holder.mTitleView.getContext(), R.color.colorAccent),
                        mPadding),
                0, titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.mTitleView.setText(titleSpan);

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

        // Get and set text for category

        holder.mCategoryView.setText(holder.mItem.getSection());

        // Set the url link for this article item

        holder.mBrowserLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openUrlInBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(holder.mItem.getArticleURL()));
                holder.mBrowserLinkView.getContext().startActivity(openUrlInBrowser);
            }
        });

        //Get and set the date

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String parsedDate = null;

        try {
            Date date = dateFormat.parse(holder.mItem.getDatePublished());
            DateFormat newDateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm");
            parsedDate = newDateFormat.format(date);

        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date.", e);
            e.printStackTrace();
        }
        holder.mDateView.setText(parsedDate);

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

    // Override getItemViewType to prevent the random switch of images when scrolling.
    @Override
    public int getItemViewType(int position) { return position; }

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
        final TextView mCategoryView;
        final TextView mBrowserLinkView;
        final TextView mDateView;
        final Integer mThumbnailViewId;
        final ImageView mThumbnailView;
        final ProgressBar thumbnailProgressBar;
        NewsItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mAuthorView = (TextView) view.findViewById(R.id.author);
            mCategoryView = (TextView) view.findViewById(R.id.category);
            mBrowserLinkView = (TextView) view.findViewById(R.id.browserLink);
            mDateView = (TextView) view.findViewById(R.id.date);
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