/**
 * Copyright 2015 Robert Vorkoeper - robert-vor@gmx.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.util;

import com.keybox.common.util.AppConfig;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * sets applications keys on systems with given refresh interval
 */
public class RefreshApplicationKeyUtil {


	private Timer timer;
	private static Integer days = Integer.valueOf(AppConfig.getProperty("dynamicKeyRotation"));


	private RefreshApplicationKeyUtil() {
		timer = new Timer();
				
		//Execution at start
		timer.schedule(new RefreshApplicationKeyTimerTask(), Calendar.getInstance().getTime());
		
		//From tomorrow, every day by 1 clock
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 1);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set( Calendar.MILLISECOND, 0 );
		timer.schedule(new RefreshApplicationKeyTimerTask(), start.getTime(), 24 * 60 * 60 * 1000);
	}

	/**
	 * start timer to refresh all systems
	 */
	public static void startRefreshAllSystemsTimerTask() {
		if (SSHUtil.dynamicKeys && days > 0) {
			new RefreshApplicationKeyUtil();
		}
	}
	
	public static boolean getDynamicKeyRotation() {
		if(days>0)
		{
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Timer task for refresh old Application keys on systems
	 */
	class RefreshApplicationKeyTimerTask extends TimerTask {

	@Override
	public void run() {	
		SSHUtil.refreshApplicationKey(RefreshApplicationKeyUtil.days);
		}
	}
}
