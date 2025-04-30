package org.example;

import java.util.concurrent.locks.*;

/**
 * This class handles concurrency control using read/write locks.
 */
public class ConcurrencyControl {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Acquires a read lock, allowing multiple read operations to occur simultaneously.
     */
    public void acquireReadLock() {
        lock.readLock().lock();
    }

    /**
     * Releases the read lock.
     */
    public void releaseReadLock() {
        lock.readLock().unlock();
    }

    /**
     * Acquires a write lock, ensuring exclusive access for write operations.
     */
    public void acquireWriteLock() {
        lock.writeLock().lock();
    }

    /**
     * Releases the write lock.
     */
    public void releaseWriteLock() {
        lock.writeLock().unlock();
    }
}
