/**
 * Copyright 2014 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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
