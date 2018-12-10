/**
 *    Copyright (C) 2014 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import java.util.Timer;
import java.util.TimerTask;

/**
 * sets authorized keys on systems with given refresh interval
 */
public class RefreshAuthKeyUtil {


	private Timer timer;
	private static Integer minute = Integer.valueOf(AppConfig.getProperty("authKeysRefreshInterval"));


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
			SSHUtil.distributePubKeysToAllSystems();
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

	Long userId;
	public RefreshUserSystemsTask(Long userId){
		this.userId=userId;
	}

	@Override
	public void run() {
		//distribute all public keys based on user
		SSHUtil.distributePubKeysToUser(this.userId);
	}
}


/**
 * Task for distributing keys to all systems assigned to a profile
 */
class RefreshProfileSystemsTask implements Runnable {

	Long profileId;
	public RefreshProfileSystemsTask(Long profileId){
		this.profileId=profileId;
	}

	@Override
	public void run() {
		//distribute all public keys based on profile
		SSHUtil.distributePubKeysToProfile(this.profileId);
	}
}


/**
 * Task for distributing keys to all systems
 */
class RefreshAllSystemsTask implements Runnable {

	@Override
	public void run() {
		//distribute all public keys
		SSHUtil.distributePubKeysToAllSystems();

	}
}
