package com.turn.browser.utils;


import com.turn.browser.enums.AppStatus;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @description: Program status management tool
 **/
public class AppStatusUtil {

    private static final ReentrantReadWriteLock APP_STATUS_LOCK = new ReentrantReadWriteLock();

    private AppStatusUtil() {
    }

    private static AppStatus appStatus;

    /**
     * Set application status
     *
     * @param status
     */
    public static void setStatus(AppStatus status) {
        APP_STATUS_LOCK.writeLock().lock();
        try {
            appStatus = status;
        } finally {
            APP_STATUS_LOCK.writeLock().unlock();
        }
    }

    /**
     * Get application status
     *
     * @return
     */
    public static AppStatus getStatus() {
        APP_STATUS_LOCK.readLock().lock();
        try {
            return appStatus;
        } finally {
            APP_STATUS_LOCK.readLock().unlock();
        }
    }

    /**
     * Whether the application is in the startup process
     *
     * @return
     */
    public static boolean isBooting() {
        APP_STATUS_LOCK.readLock().lock();
        try {
            return appStatus == AppStatus.BOOTING;
        } finally {
            APP_STATUS_LOCK.readLock().unlock();
        }
    }

    /**
     * Whether the application is running normally
     *
     * @return
     */
    public static boolean isRunning() {
        APP_STATUS_LOCK.readLock().lock();
        try {
            return appStatus == AppStatus.RUNNING;
        } finally {
            APP_STATUS_LOCK.readLock().unlock();
        }
    }

    /**
     * Whether the application has been stopped
     *
     * @return
     */
    public static boolean isStopped() {
        APP_STATUS_LOCK.readLock().lock();
        try {
            return appStatus == AppStatus.STOPPED;
        } finally {
            APP_STATUS_LOCK.readLock().unlock();
        }
    }

}