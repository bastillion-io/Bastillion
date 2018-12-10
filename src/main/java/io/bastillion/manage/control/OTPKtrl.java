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
package io.bastillion.manage.control;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.util.OTPUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;

public class OTPKtrl extends BaseKontroller {

    public final static boolean requireOTP = "required".equals(AppConfig.getProperty("oneTimePassword"));
    //QR image size
    private static final int QR_IMAGE_WIDTH = 325;
    private static final int QR_IMAGE_HEIGHT = 325;
    private static Logger log = LoggerFactory.getLogger(OTPKtrl.class);
    @Model(name = "qrImage")
    String qrImage;
    @Model(name = "sharedSecret")
    String sharedSecret;

    public OTPKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/viewOTP", method = MethodType.GET)
    public String viewOTP() {

        sharedSecret = OTPUtil.generateSecret();

        AuthUtil.setOTPSecret(getRequest().getSession(), sharedSecret);

        qrImage = Long.toString(new Date().getTime()) + ".png";

        return "/admin/two-factor_otp.html";

    }


    @Kontrol(path = "/admin/otpSubmit", method = MethodType.POST)
    public String otpSubmit() {

        AuthDB.updateSharedSecret(sharedSecret, AuthUtil.getAuthToken(getRequest().getSession()));

        if (requireOTP) {
            AuthUtil.deleteAllSession(getRequest().getSession());
        }
        return "redirect:/logout.ktrl";

    }


    @Kontrol(path = "/admin/qrImage", method = MethodType.GET)
    public String qrImage() {

        String username = UserDB.getUser(AuthUtil.getUserId(getRequest().getSession())).getUsername();

        String secret = AuthUtil.getOTPSecret(getRequest().getSession());

        AuthUtil.setOTPSecret(getRequest().getSession(), null);

        try {

            String qrCodeText = "otpauth://totp/Bastillion%20%28" + URLEncoder.encode(getRequest().getHeader("host").replaceAll("\\:.*$", ""), "utf-8") + "%29:" + username + "?secret=" + secret;

            QRCodeWriter qrWriter = new QRCodeWriter();

            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");


            BitMatrix matrix = qrWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT, hints);
            getResponse().setContentType("image/png");

            BufferedImage image = new BufferedImage(QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT);
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < QR_IMAGE_WIDTH; x++) {
                for (int y = 0; y < QR_IMAGE_HEIGHT; y++) {
                    if (matrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            ImageIO.write(image, "png", getResponse().getOutputStream());

            getResponse().getOutputStream().flush();
            getResponse().getOutputStream().close();

        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }

        return null;

    }
}
