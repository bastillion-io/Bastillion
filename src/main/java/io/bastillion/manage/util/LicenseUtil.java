package io.bastillion.manage.util;

import io.bastillion.manage.db.LicenseDB;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
				int index = str.lastIndexOf("-");
				Date expirationDt = sdf.parse(str.substring(index + 1));
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
	 *
	 * @param license encrypted license string
	 * @return license expiration
	 */
	public static String getExpirationDt(String license) {

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String str = EncryptionUtil.decrypt(k, license);
		if (StringUtils.isNotEmpty(str) && str.contains("@") && str.contains("-")) {
			try {
				int index = str.lastIndexOf("-");
				sdf.parse(str.substring(index + 1));
				return str.substring(index + 1);
			} catch (ParseException ex) {
			}
		}
		return null;
	}

	/**
	 * returns the license for running in ec2
	 *
	 * @return license text
	 */
	public static String generateForEC2() {
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.YEAR, 50);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		return EncryptionUtil.encrypt(k, "ec2@bastillion.io-" + sdf.format(cal.getTime()));

	}

}
