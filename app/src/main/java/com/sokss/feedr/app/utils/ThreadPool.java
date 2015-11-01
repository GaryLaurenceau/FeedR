package com.sokss.feedr.app.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.sokss.feedr.app.FeedActivity;
import com.sokss.feedr.app.model.News;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gary on 10/02/15.
 */
public class ThreadPool {

    private ExecutorService mExecutor = Executors.newFixedThreadPool(5);

    private static ThreadPool ourInstance = new ThreadPool();

    public static ThreadPool getInstance() {
        return ourInstance;
    }

    private ThreadPool() {
    }

    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    public void shutdown() {
        mExecutor.shutdown();
    }

    static abstract public class Worker implements Runnable {

        public Worker(){
        }

        @Override
        public void run() {
            command();
        }

        abstract public void command();
    }
}
