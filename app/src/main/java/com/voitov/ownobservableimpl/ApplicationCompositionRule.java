package com.voitov.ownobservableimpl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApplicationCompositionRule {
    private Handler uiHandler;
    private ThreadPoolExecutor poolExecutor;

    public Handler getUiHandler() {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
        return uiHandler;
    }

    public ThreadPoolExecutor getPoolExecutor() {
        if (poolExecutor == null) {
            poolExecutor = new ThreadPoolExecutor(
                    0,
                    Integer.MAX_VALUE,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable runnable) {
                            Log.d("ProduceConsumerUseCase",
                                    String.format("size: %s, active: %s, remaining: %s",
                                            poolExecutor.getPoolSize(),
                                            poolExecutor.getActiveCount(),
                                            poolExecutor.getQueue().remainingCapacity()));
                            return new Thread(runnable);
                        }
                    }
            );
        }
        return poolExecutor;
    }
}
