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
package com.keybox.manage.action;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.util.OTPUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InterceptorRef("keyboxStack")
public class OTPAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static Logger log = LoggerFactory.getLogger(OTPAction.class);
    public final static boolean requireOTP = "required".equals(AppConfig.getProperty("oneTimePassword"));

    //QR image size
    private static final int QR_IMAGE_WIDTH = 325;
    private static final int QR_IMAGE_HEIGHT = 325;

    HttpServletRequest servletRequest;
    HttpServletResponse servletResponse;
    String qrImage;
    String sharedSecret;

    @Action(value = "/admin/viewOTP",
            results = {
                    @Result(name = "success", location = "/admin/two-factor_otp.jsp"),
                    @Result(name = "error", location = "/login.action", type = "redirect")
            }
    )
    public String viewOTP() {
        
        sharedSecret = OTPUtil.generateSecret();
        
        AuthUtil.setOTPSecret(servletRequest.getSession(), sharedSecret);
        
        this.setQrImage(Long.toString(new Date().getTime()) + ".png");

        return SUCCESS;

    }


    @Action(value = "/admin/otpSubmit",
            results = {
                    @Result(name = "success", location = "/logout.action", type = "redirect")
            }
    )
    public String otpSubmit() {

        AuthDB.updateSharedSecret(sharedSecret, AuthUtil.getAuthToken(servletRequest.getSession()));

        if (requireOTP) {
            AuthUtil.deleteAllSession(servletRequest.getSession());
        }
        return SUCCESS;

    }


    @Action(value = "/admin/qrImage")
    public String qrImage() {

        String username = UserDB.getUser(AuthUtil.getUserId(servletRequest.getSession())).getUsername();

        String secret = AuthUtil.getOTPSecret(servletRequest.getSession());
        
        AuthUtil.setOTPSecret(servletRequest.getSession(), null);

        try {

            String qrCodeText = "otpauth://totp/KeyBox%20%28" + URLEncoder.encode(servletRequest.getHeader("host").replaceAll("\\:.*$",""), "utf-8") + "%29:" + username + "?secret=" + secret;

            QRCodeWriter qrWriter = new QRCodeWriter();

            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");


            BitMatrix matrix = qrWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT, hints);
            servletResponse.setContentType("image/png");

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
            ImageIO.write(image, "png", servletResponse.getOutputStream());
            
            servletResponse.getOutputStream().flush();
            servletResponse.getOutputStream().close();
            
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }

        return null;

    }

    public String getQrImage() {
        return qrImage;
    }

    public void setQrImage(String qrImage) {
        this.qrImage = qrImage;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
}
