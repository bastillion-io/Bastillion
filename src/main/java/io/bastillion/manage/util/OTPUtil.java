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


import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Time-based One-Time Password Utility
 */
public class OTPUtil {

    private static Logger log = LoggerFactory.getLogger(OTPUtil.class);

    //sizes to generate OTP secret
    private static final int SECRET_SIZE = 10;
    private static final int NUM_SCRATCH_CODES = 5;
    private static final int SCRATCH_CODE_SIZE = 4;

    //token window in near future or past
    private static final int TOKEN_WINDOW = 3;

    //interval for validation token change
    private static final int CHANGE_INTERVAL = 30;

    private OTPUtil() {
    }


    /**
     * generates OPT secret
     *
     * @return String shared secret
     */
    public static String generateSecret() {
        byte[] buffer = new byte[(NUM_SCRATCH_CODES * SCRATCH_CODE_SIZE) + SECRET_SIZE];
        new SecureRandom().nextBytes(buffer);

        byte[] secret = Arrays.copyOf(buffer, SECRET_SIZE);

        return new String(new Base32().encode(secret));

    }

    /**
     * verifies code for OTP secret
     *
     * @param secret shared secret
     * @param token  verification token
     * @return true if success
     */
    public static boolean verifyToken(String secret, long token) {

        //check token in near future or past
        int window = TOKEN_WINDOW;
        for (int i = window; i >= -window; i--) {

            long time = (new Date().getTime() / TimeUnit.SECONDS.toMillis(CHANGE_INTERVAL)) + i;

            if (verifyToken(secret, token, time)) {
                return true;
            }
        }

        return false;

    }

    /**
     * verifies code for OTP secret per time interval
     *
     * @param secret shared secret
     * @param token  verification token
     * @param time   time representation to calculate OTP
     * @return true if success
     */
    private static boolean verifyToken(String secret, long token, long time) {

        long calculated = -1;

        byte[] key = new Base32().decode(secret);

        SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA1");

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(time).array());

            int offset = hash[hash.length - 1] & 0xF;
            for (int i = 0; i < 4; ++i) {
                calculated <<= 8;
                calculated |= (hash[offset + i] & 0xFF);
            }

            calculated &= 0x7FFFFFFF;
            calculated %= 1000000;
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }


        return (calculated != -1 && calculated == token);


    }

}
