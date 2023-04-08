package com.voitov.ownobservableimpl;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simplified implementation of blocking queue.
 * Author: Vladislav Voitov, dobrihlopez@gmail.com
 */

public class MyBlockingQueue<T> {
    public static final Object QUEUE_LOCK = new Object();
    private static final int DEFAULT_CAPACITY = 7;
    private final int capacity;
    private int currentSize = 0;
    private final Queue<T> myQueue = new LinkedList<>();

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public MyBlockingQueue() {
        capacity = DEFAULT_CAPACITY;
    }

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * for space to become available.
     *
     * @param item the element to add
     */

    public void put(T item) {
        synchronized (QUEUE_LOCK) {
            while (currentSize == capacity) {
                try {
                    QUEUE_LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            myQueue.add(item);
            currentSize++;
            QUEUE_LOCK.notifyAll();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     *
     * @return the head of this queue
     */
    public T take() {
        synchronized (QUEUE_LOCK) {
            while (currentSize == 0) {
                try {
                    QUEUE_LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            currentSize--;
            QUEUE_LOCK.notifyAll();
            return myQueue.poll();
        }
    }
}
