package com.voitov.ownobservableimpl;

import static com.voitov.ownobservableimpl.ProduceConsumerBenchmarkUseCase.OnBenchmarkListener;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProduceConsumerBenchmarkUseCase extends BaseObservable<OnBenchmarkListener> {
    private static final int MILLIS_IN_SECONDS = 1000;
    private final int NUM_OF_MESSAGES_TO_RECEIVE = 10000;
    private final int NUM_OF_MESSAGES_TO_BE_SENT = 10000;
    private final Object PRODUCER_CONSUMER_LOCK = new Object();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Object syncPoolExecutor = new Object();
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    synchronized (syncPoolExecutor) {
                        Log.d("ProduceConsumerUseCase",
                                String.format("size: %s, active: %s, remaining: %s",
                                        poolExecutor.getPoolSize(),
                                        poolExecutor.getActiveCount(),
                                        poolExecutor.getQueue().remainingCapacity()));
                    }
                    return new Thread(runnable);
                }
            }
    );
    //private final ExecutorService poolExecutor = Executors.newCachedThreadPool();

    private MyBlockingQueue<String> queue;
    private long startExecutionBenchmarkTimestamp;
    private int numOfReceivedMessages;

    public static interface OnBenchmarkListener {
        public void onBenchmarkCompleted(ScreenState state);
    }

    public void startBenchmarkingWithCallback() {
        init();
        startManagerAndNotify();
    }

    private void init() {
        numOfReceivedMessages = 0;
        queue = new MyBlockingQueue<String>();
    }

    private void startManagerAndNotify() {
        startExecutionBenchmarkTimestamp = System.currentTimeMillis();

        //report watcher thread
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PRODUCER_CONSUMER_LOCK) {
                    while (numOfReceivedMessages < NUM_OF_MESSAGES_TO_RECEIVE) {
                        try {
                            PRODUCER_CONSUMER_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    notifySuccess();
                }
            }
        });

        //consumer thread
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NUM_OF_MESSAGES_TO_RECEIVE; i++) {
                    startMessageConsumer();
                }
            }
        });

        //producer thread
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < NUM_OF_MESSAGES_TO_BE_SENT; i++) {
                    startMessageProducer(i);
                }
            }
        });
    }

    private void startMessageConsumer() {
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String message;
                try {
                    message = queue.take();
                    //do something with data being received from queue
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (PRODUCER_CONSUMER_LOCK) {
                    numOfReceivedMessages++;
                    PRODUCER_CONSUMER_LOCK.notifyAll();
                }
            }
        });
    }

    private void startMessageProducer(final int index) {
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                queue.put(index + " some message");
            }
        });
    }

    private void notifySuccess() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = getElapsedSeconds();
                int receivedMessages = numOfReceivedMessages;

                ScreenState state = new ScreenState.ComputationCompleted(seconds, receivedMessages);
                for (OnBenchmarkListener listener : getListeners()) {
                    listener.onBenchmarkCompleted(state);
                }
            }
        });
    }

    private int getElapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startExecutionBenchmarkTimestamp) / MILLIS_IN_SECONDS);
    }
}
