package io.bastillion.manage.util;

import io.bastillion.manage.db.LicenseDB;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LicenseUtil {

	private static byte[] k = {73, 46, 120, -102,
			-57, 108, 1, -37,
			87, -45, 33, -127,
			-106, -101, -54, -75};

	private LicenseUtil() {
	}


	/**
	 * checks for a valid license
	 *
	 * @return true if valid license
	 */
	public static boolean isValid() {
		return isValid(LicenseDB.getLicense());
	}

	/**
	 * checks for a valid license
	 *
	 * @param license encrypted license string
	 * @return true if valid license
	 */
	public static boolean isValid(String license) {

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String str = EncryptionUtil.decrypt(k, license);
		if (StringUtils.isNotEmpty(str) && str.contains("@") && str.contains("-")) {
			try {
				Date expirationDt = sdf.parse(str.split("-")[1]);
				if (expirationDt.before(Calendar.getInstance().getTime())) {
					return false;
				}
			} catch (ParseException ex) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * returns the license expiration
	 * @param license encrypted license string
	 * @return license expiration
	 */
	public static String getExpirationDt(String license) {

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String str = EncryptionUtil.decrypt(k, license);
		if (StringUtils.isNotEmpty(str) && str.contains("@") && str.contains("-")) {
			try {
				sdf.parse(str.split("-")[1]);
				return str.split("-")[1];
			} catch (ParseException ex) {
			}
		}
		return null;
	}
}