package com.voitov.ownobservableimpl;

import android.util.Log;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApplicationCompositionRule {
    private ThreadPoolExecutor poolExecutor;

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
