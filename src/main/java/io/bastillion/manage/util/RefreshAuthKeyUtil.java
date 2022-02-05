/**
 * Copyright (C) 2014 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * sets authorized keys on systems with given refresh interval
 */
public class RefreshAuthKeyUtil {

    private static final Logger log = LoggerFactory.getLogger(RefreshAuthKeyUtil.class);

    private Timer timer;
    private static final Integer minute = Integer.valueOf(AppConfig.getProperty("authKeysRefreshInterval"));


    private RefreshAuthKeyUtil() {
        //set interval
        timer = new Timer();
        timer.schedule(new RefreshAllSystemsTimerTask(), minute * 60 * 1000);
    }

    /**
     * start timer to refresh all systems
     */
    public static void startRefreshAllSystemsTimerTask() {
        if (SSHUtil.keyManagementEnabled && minute > 0) {
            new RefreshAuthKeyUtil();
        }
    }

    /**
     * Task for distributing keys to all systems
     */
    public static void refreshAllSystems() {

        Runnable run = new RefreshAllSystemsTask();
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * Task for distributing keys to all systems based on profile
     *
     * @param profileId profile id
     */
    public static void refreshProfileSystems(Long profileId) {

        Runnable run = new RefreshProfileSystemsTask(profileId);
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     *Task for distributing keys to all systems based on user
     *
     * @param userId user id
     */
    public static void refreshUserSystems(Long userId) {

        Runnable run = new RefreshUserSystemsTask(userId);
        Thread thread = new Thread(run);
        thread.start();

    }


    /**
     * Timer task for distributing keys to all systems
     */
    private class RefreshAllSystemsTimerTask extends TimerTask {

        @Override
        public void run() {
            //distribute all public keys
            try {
                SSHUtil.distributePubKeysToAllSystems();
            } catch (SQLException | GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
            }
            timer.cancel();

            //create new timer and set interval
            timer = new Timer();
            timer.schedule(new RefreshAllSystemsTimerTask(), minute * 60 * 1000);
        }
    }

}


/**
 * Task for distributing keys to all systems based on user
 */
class RefreshUserSystemsTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshUserSystemsTask.class);

    Long userId;

    public RefreshUserSystemsTask(Long userId) {
        this.userId = userId;
    }

    @Override
    public void run() {
        //distribute all public keys based on user
        try {
            SSHUtil.distributePubKeysToUser(this.userId);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }
}


/**
 * Task for distributing keys to all systems assigned to a profile
 */
class RefreshProfileSystemsTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshProfileSystemsTask.class);

    Long profileId;

    public RefreshProfileSystemsTask(Long profileId) {
        this.profileId = profileId;
    }

    @Override
    public void run() {
        //distribute all public keys based on profile
        try {
            SSHUtil.distributePubKeysToProfile(this.profileId);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }
}


/**
 * Task for distributing keys to all systems
 */
class RefreshAllSystemsTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshAllSystemsTask.class);

    @Override
    public void run() {
        //distribute all public keys
        try {
            SSHUtil.distributePubKeysToAllSystems();
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }

    }
}
