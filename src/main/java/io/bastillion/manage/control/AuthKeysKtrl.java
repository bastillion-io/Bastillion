/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.PublicKeyDB;
import io.bastillion.manage.db.SessionAuditDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.PublicKey;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.EncryptionUtil;
import io.bastillion.manage.util.PasswordUtil;
import io.bastillion.manage.util.RefreshAuthKeyUtil;
import io.bastillion.manage.util.SSHUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
public class AuthKeysKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(AuthKeysKtrl.class);

    private static final String REQUIRED = "Required";
    private static final String INVALID = "Invalid";
    private static final String PUBLIC_KEY_PUBLIC_KEY = "publicKey.publicKey";
    private static final String PVT_KEY = "privateKey";

    @Model(name = "profileList")
    List<Profile> profileList = new ArrayList<>();
    @Model(name = "userList")
    List userList = new ArrayList<>();
    @Model(name = "publicKey")
    PublicKey publicKey;
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "forceUserKeyGenEnabled")
    boolean forceUserKeyGenEnabled = "true".equals(AppConfig.getProperty("forceUserKeyGeneration"));
    @Model(name = "hostSystem")
    HostSystem hostSystem = new HostSystem();
    @Model(name = "userPublicKeyList")
    List<PublicKey> userPublicKeyList = new ArrayList<>();
    @Model(name = "existingKeyId")
    Long existingKeyId;


    public AuthKeysKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/enablePublicKey", method = MethodType.GET)
    public String enablePublicKey() throws ServletException {

        try {
            publicKey = PublicKeyDB.getPublicKey(publicKey.getId());
            PublicKeyDB.enableKey(publicKey.getId());

            profileList = ProfileDB.getAllProfiles();
            userList = UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        distributePublicKeys(publicKey);

        return "/manage/view_keys.html";
    }

    @Kontrol(path = "/manage/disablePublicKey", method = MethodType.GET)
    public String disablePublicKey() throws ServletException {

        try {
            publicKey = PublicKeyDB.getPublicKey(publicKey.getId());

            PublicKeyDB.disableKey(publicKey.getId());

            profileList = ProfileDB.getAllProfiles();
            userList = UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        distributePublicKeys(publicKey);

        return "/manage/view_keys.html";
    }

    @Kontrol(path = "/manage/viewKeys", method = MethodType.GET)
    public String manageViewKeys() throws ServletException {

        try {
            profileList = ProfileDB.getAllProfiles();
            userList = UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();
            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return "/manage/view_keys.html";
    }

    @Kontrol(path = "/admin/viewKeys", method = MethodType.GET)
    public String adminViewKeys() throws ServletException {

        try {
            Long userId = AuthUtil.getUserId(getRequest().getSession());
            String userType = AuthUtil.getUserType(getRequest().getSession());
            if (Auth.MANAGER.equals(userType)) {
                profileList = ProfileDB.getAllProfiles();
            } else {
                profileList = UserProfileDB.getProfilesByUser(userId);
            }
            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);

            userPublicKeyList = PublicKeyDB.getUniquePublicKeysForUser(userId);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return "/admin/view_keys.html";
    }

    @Kontrol(path = "/admin/savePublicKey", method = MethodType.POST)
    public String savePublicKeys() throws ServletException {
        try {
            Long userId = AuthUtil.getUserId(getRequest().getSession());
            String userType = AuthUtil.getUserType(getRequest().getSession());

            publicKey.setUserId(userId);
            if (Auth.MANAGER.equals(userType) || UserProfileDB.checkIsUsersProfile(userId, publicKey.getProfile().getId())) {
                if (publicKey.getId() != null) {
                    PublicKeyDB.updatePublicKey(publicKey);
                } else {
                    PublicKeyDB.insertPublicKey(publicKey);
                }
                distributePublicKeys(publicKey);
            }
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return "redirect:/admin/viewKeys.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField() + "&keyNm=" + publicKey.getKeyNm();
    }

    @Kontrol(path = "/admin/deletePublicKey", method = MethodType.GET)
    public String deletePublicKey() throws ServletException {
        if (publicKey.getId() != null) {

            try {
                //get public key then delete
                publicKey = PublicKeyDB.getPublicKey(publicKey.getId());
                PublicKeyDB.deletePublicKey(publicKey.getId(), AuthUtil.getUserId(getRequest().getSession()));
            } catch (SQLException | GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }
        }

        distributePublicKeys(publicKey);

        return "redirect:/admin/viewKeys.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }

    @Kontrol(path = "/admin/downloadPvtKey", method = MethodType.GET)
    public String downloadPvtKey() throws ServletException {

        String privateKey = null;
        try {
            privateKey = EncryptionUtil.decrypt((String) getRequest().getSession().getAttribute(PVT_KEY));
        } catch (GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        if (StringUtils.isNotEmpty(publicKey.getKeyNm()) && StringUtils.isNotEmpty(privateKey)) {

            try {
                getResponse().setContentType("application/octet-stream");
                getResponse().setHeader("Content-Disposition", "attachment;filename=" + publicKey.getKeyNm() + ".key");
                getResponse().getOutputStream().write(privateKey.getBytes());
                getResponse().getOutputStream().flush();
                getResponse().getOutputStream().close();
            } catch (IOException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }

        }
        //remove pvt key
        getRequest().getSession().setAttribute(PVT_KEY, null);
        getRequest().getSession().removeAttribute(PVT_KEY);

        return null;
    }

    /**
     * generates public private key from passphrase
     *
     * @param username username to set in public key comment
     * @param keyname  keyname to set in public key comment
     * @return public key
     */
    public String generateUserKey(String username, String keyname) throws ServletException {

        //set key type
        int type = KeyPair.RSA;
        if ("dsa".equals(SSHUtil.KEY_TYPE)) {
            type = KeyPair.DSA;
        } else if ("ecdsa".equals(SSHUtil.KEY_TYPE)) {
            type = KeyPair.ECDSA;
        } else if ("ed25519".equals(SSHUtil.KEY_TYPE)) {
            type = KeyPair.ED25519;
        } else if ("ed448".equals(SSHUtil.KEY_TYPE)) {
            type = KeyPair.ED448;
        }

        JSch jsch = new JSch();

        String pubKey;

        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, type, SSHUtil.KEY_LENGTH);

            OutputStream os = new ByteArrayOutputStream();
            keyPair.writePrivateKey(os, publicKey.getPassphrase().getBytes());
            //set private key
            try {
                getRequest().getSession().setAttribute(PVT_KEY, EncryptionUtil.encrypt(os.toString()));
            } catch (GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }

            os = new ByteArrayOutputStream();
            keyPair.writePublicKey(os, username + "@" + keyname);
            pubKey = os.toString();


            keyPair.dispose();
        } catch (JSchException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return pubKey;
    }

    /**
     * Validates all fields for adding a public key
     */
    @Validate(input = "/admin/view_keys.html")
    public void validateSavePublicKeys() throws ServletException {

        Long userId = null;
        try {
            userId = AuthUtil.getUserId(getRequest().getSession());
        } catch (GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        if (publicKey == null
                || publicKey.getKeyNm() == null
                || publicKey.getKeyNm().trim().equals("")) {
            addFieldError("publicKey.keyNm", REQUIRED);

        }

        try {
            if (publicKey != null) {
                if (existingKeyId != null) {
                    publicKey.setPublicKey(PublicKeyDB.getPublicKey(existingKeyId).getPublicKey());
                } else if ("true".equals(AppConfig.getProperty("forceUserKeyGeneration"))) {
                    if (publicKey.getPassphrase() == null ||
                            publicKey.getPassphrase().trim().equals("")) {
                        addFieldError("publicKey.passphrase", REQUIRED);
                    } else if (publicKey.getPassphraseConfirm() == null ||
                            publicKey.getPassphraseConfirm().trim().equals("")) {
                        addFieldError("publicKey.passphraseConfirm", REQUIRED);
                    } else if (!publicKey.getPassphrase().equals(publicKey.getPassphraseConfirm())) {
                        addError("Passphrases do not match");
                    } else if (!PasswordUtil.isValid(publicKey.getPassphrase())) {
                        addError(PasswordUtil.PASSWORD_REQ_ERROR_MSG);
                    } else {
                        publicKey.setPublicKey(generateUserKey(UserDB.getUser(userId).getUsername(), publicKey.getKeyNm()));
                    }
                }

                if (publicKey.getPublicKey() == null || publicKey.getPublicKey().trim().equals("")) {
                    addFieldError(PUBLIC_KEY_PUBLIC_KEY, REQUIRED);

                } else if (SSHUtil.getFingerprint(publicKey.getPublicKey()) == null || SSHUtil.getKeyType(publicKey.getPublicKey()) == null) {
                    addFieldError(PUBLIC_KEY_PUBLIC_KEY, INVALID);

                } else if (PublicKeyDB.isKeyDisabled(SSHUtil.getFingerprint(publicKey.getPublicKey()))) {
                    addError("This key has been disabled. Please generate and set a new public key.");
                    addFieldError(PUBLIC_KEY_PUBLIC_KEY, INVALID);

                } else if (PublicKeyDB.isKeyRegistered(userId, publicKey)) {
                    addError("This key has already been registered under selected profile.");
                    addFieldError(PUBLIC_KEY_PUBLIC_KEY, INVALID);

                }
            }

            if (!this.getFieldErrors().isEmpty()) {

                String userType = AuthUtil.getUserType(getRequest().getSession());

                if (Auth.MANAGER.equals(userType)) {
                    profileList = ProfileDB.getAllProfiles();
                } else {
                    profileList = UserProfileDB.getProfilesByUser(userId);
                }

                sortedSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);
                userPublicKeyList = PublicKeyDB.getUniquePublicKeysForUser(userId);
            }
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

    }


    /**
     * distribute public keys to all systems or to profile
     *
     * @param publicKey public key to distribute
     */
    private void distributePublicKeys(PublicKey publicKey) {

        if (publicKey.getProfile() != null && publicKey.getProfile().getId() != null) {
            RefreshAuthKeyUtil.refreshProfileSystems(publicKey.getProfile().getId());
        } else {
            RefreshAuthKeyUtil.refreshAllSystems();
        }

    }
}
