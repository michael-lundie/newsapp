package com.michaellundie.newsapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Async task for download images.
 * Image Async Downloader code primarily from:
 * https://android.jlelse.eu/async-loading-images-on-android-like-a-big-baws-fd97d1a91374
 */
public class DownloadImageAsync extends AsyncTask<String, Void, Bitmap> {

    private static final String LOG_TAG = DownloadImageAsync.class.getSimpleName();

    private Listener listener;
    DownloadImageAsync(final Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onImageDownloaded(final Bitmap bitmap);
        void onImageDownloadError();
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Log.i(LOG_TAG, "TEST: download url:" + urls);
        final String url = urls[0];
        Bitmap bitmap = null;
        try {
            final InputStream inputStream = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (final MalformedURLException malformedUrlException) {
            Log.e(LOG_TAG, "There was a problem with the URL.", malformedUrlException);
        } catch (final IOException ioException) {
            Log.e(LOG_TAG, "There was a problem loading the requested image.", ioException);
        }
        return bitmap;
    }
    @Override
    protected void onPostExecute(Bitmap downloadedBitmap) {
        if (null != downloadedBitmap) {
            listener.onImageDownloaded(downloadedBitmap);
        } else {
            listener.onImageDownloadError();
        }
    }
}