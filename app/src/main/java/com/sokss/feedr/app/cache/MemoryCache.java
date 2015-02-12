package com.sokss.feedr.app.cache;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by gary on 27/11/14.
 */
public class MemoryCache {

    private static final String TAG = "com.sokss.feedr.app.utils.MemoryCache";

    // CACHE
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskCache diskCache;
    private static final String CACHE_DIR = "/feedr";
    private File cacheDir;

    private static MemoryCache ourInstance = new MemoryCache();

    public static MemoryCache getInstance() {
        return ourInstance;
    }

    private MemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        try {
            cacheDir = new File(Environment.getExternalStorageDirectory() + CACHE_DIR);
            new File(cacheDir, DiskLruCache.JOURNAL_FILE);
            new File(cacheDir, DiskLruCache.JOURNAL_FILE_BACKUP);
            diskCache = DiskCache.open(cacheDir, 1, Integer.MAX_VALUE);
        }
        catch (IOException ioe) {
            Log.d(TAG, "IOException");
            Log.e(TAG, ioe.toString());
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            return;
        }
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
        try {
            if (diskCache == null) {
                return;
            }
            if (diskCache.getBitmap(key) == null) {
                Log.d("key", key);
                diskCachePutBitmap(key, bitmap);
            }
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        catch (OutOfMemoryError oom) {
            System.gc();
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        Bitmap bitmap = null;
        if (mMemoryCache == null)
            return null;
        try {
            bitmap = mMemoryCache.get(key);
            if (bitmap == null)
                bitmap = diskCacheGetBitmap(key);
        }
        catch (OutOfMemoryError oom) {
            mMemoryCache.evictAll();
            System.gc();
            Log.e(TAG, oom.toString());
        }
        return bitmap;
    }

    private void diskCachePutBitmap(String key, Bitmap bitmap) {
        if (diskCache == null) {
            return;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();
        InputStream bs = new ByteArrayInputStream(bitmapdata);
        try {
            diskCache.put(diskCache.toInternalKey(key), bs);
        }
        catch (Exception ioe) {
            Log.e(TAG, ioe.toString());
        }
    }

    private Bitmap diskCacheGetBitmap(String key) {
        if (diskCache == null)
            return null;
        Bitmap bitmap = null;
        try {
            DiskCache.BitmapEntry entry = diskCache.getBitmap(diskCache.toInternalKey(key));
            if (entry == null) {
                return null;
            }
            bitmap = entry.getBitmap();
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        return bitmap;
    }

    public void flushCache() {
        try {
            diskCache.clear();
        }
        catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
    }
}
