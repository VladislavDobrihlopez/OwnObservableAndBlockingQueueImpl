package com.voitov.ownobservableimpl;

import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import io.reactivex.Observable;

public class ProduceConsumerBenchmarkUseCase {
    private static final int MILLIS_IN_SECONDS = 1000;
    private final int NUM_OF_MESSAGES_TO_RECEIVE = 10000;
    private final int NUM_OF_MESSAGES_TO_BE_SENT = 10000;
    private final Object PRODUCER_CONSUMER_LOCK = new Object();
    private final Handler handler;
    private final ThreadPoolExecutor poolExecutor;

    public ProduceConsumerBenchmarkUseCase(Handler handler, ThreadPoolExecutor threadPoolExecutor) {
        this.handler = handler;
        poolExecutor = threadPoolExecutor;
    }

    private MyBlockingQueue<String> queue;
    private long startExecutionBenchmarkTimestamp;
    private int numOfReceivedMessages;

    public static interface OnBenchmarkListener {
        public void onBenchmarkCompleted(ScreenState state);
    }

    private void init() {
        synchronized (PRODUCER_CONSUMER_LOCK) {
            startExecutionBenchmarkTimestamp = System.currentTimeMillis();
            numOfReceivedMessages = 0;
            queue = new MyBlockingQueue<String>();
        }
    }

    public Observable<ScreenState> startBenchmarking() {
        //report watcher thread
        return Observable.fromCallable(new Callable<ScreenState>() {
            @Override
            public ScreenState call() throws Exception {
                init();

                poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < NUM_OF_MESSAGES_TO_RECEIVE; i++) {
                            startMessageConsumer();
                        }
                    }
                });

                poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < NUM_OF_MESSAGES_TO_BE_SENT; i++) {
                            startMessageProducer(i);
                        }
                    }
                });

                synchronized (PRODUCER_CONSUMER_LOCK) {
                    while (numOfReceivedMessages < NUM_OF_MESSAGES_TO_RECEIVE) {
                        try {
                            PRODUCER_CONSUMER_LOCK.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    int seconds = getElapsedSeconds();
                    int receivedMessages = numOfReceivedMessages;

                    return new ScreenState.ComputationCompleted(seconds, receivedMessages);
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

    private int getElapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startExecutionBenchmarkTimestamp) / MILLIS_IN_SECONDS);
    }
}
