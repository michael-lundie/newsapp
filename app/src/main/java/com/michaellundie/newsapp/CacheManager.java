package com.michaellundie.newsapp;

import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.LruCache;

/**
 * Creates a cache manager allowing us to handle multiple images in cache and use them in
 * conjunction with a recycler viewer.
 * Cache Manager code from https://stackoverflow.com/a/22855962
 */
public class CacheManager {

    private static final String LOG_TAG = CacheManager.class.getSimpleName();
    private LruCache<Integer, BitmapDrawable> mMemoryCache;
    private static CacheManager instance;

    public static CacheManager getInstance() {
        if(instance == null) {
            // If instance is null create a new instance
            instance = new CacheManager();
            instance.init();
        }
        return instance;
    }

    private void init() {
        // Set up LruCache and assign our maximum cache size
        mMemoryCache = new LruCache<Integer, BitmapDrawable>(8 * 1024 * 1024) {
            @Override
            protected int sizeOf(Integer key, BitmapDrawable bitmapDrawable) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmapDrawable.getBitmap().getByteCount() ;
            }
            @Override
            protected void entryRemoved(boolean evicted, Integer key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    public void addBitmapToMemoryCache(Integer key, BitmapDrawable bitmapDrawable) {
        if (getBitmapFromMemCache(key) == null) {
            // Not using 'recycling drawables as was in the original code base
            mMemoryCache.put(key, bitmapDrawable);
        }
    }

    public BitmapDrawable getBitmapFromMemCache(Integer key) {
        if(key ==null) {
            Log.i(LOG_TAG, "TEST: Key is null, null will be returned.");
        }
        return mMemoryCache.get(key);
    }
    public void clear() {
        mMemoryCache.evictAll();
    }
}