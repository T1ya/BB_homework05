package ait.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueueImpl<T> implements BlkQueue<T> {
    private final LinkedList <T> queue = new LinkedList<>();
    private final int maxSize;

    private final Lock mutex = new ReentrantLock();
    private final Condition pushCondition = mutex.newCondition();
    private final Condition popCondition = mutex.newCondition();

    public BlkQueueImpl(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void push(T message) {
        mutex.lock();
        try {
            while (queue.size() == maxSize) {
                try {
                    pushCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addLast(message);
            popCondition.signal();
        }
        finally {
            mutex.unlock();
        }
    }

    @Override
    public T pop() {
        mutex.lock();
        try{
            while (queue.isEmpty()) {
                try {
                    popCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T message = queue.removeFirst();
            pushCondition.signal();
            return message;
        }
        finally {
            mutex.unlock();
        }
    }
}
