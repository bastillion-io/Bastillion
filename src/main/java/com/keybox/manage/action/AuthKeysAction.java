/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.model.*;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.PasswordUtil;
import com.keybox.manage.util.RefreshAuthKeyUtil;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to generate and distribute auth keys for systems or users
 */
@SuppressWarnings("unchecked")
public class AuthKeysAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    private static Logger log = LoggerFactory.getLogger(AuthKeysAction.class);

	HttpServletRequest servletRequest;
	HttpServletResponse servletResponse;
	List<Profile> profileList;
	List<User> userList;
	PublicKey publicKey;
	SortedSet sortedSet = new SortedSet();
	List<Long> systemSelectId;
	boolean forceUserKeyGenEnabled="true".equals(AppConfig.getProperty("forceUserKeyGeneration"));

	HostSystem pendingSystem = null;
	HostSystem hostSystem = new HostSystem();
	List<PublicKey> userPublicKeyList;
	Long existingKeyId;	



	@Action(value = "/manage/enablePublicKey",
			results = {
					@Result(name = "success", location = "/manage/view_keys.jsp")
			}
	)
	public String enablePublicKey() {

		publicKey= PublicKeyDB.getPublicKey(publicKey.getId());

		PublicKeyDB.enableKey(publicKey.getId());

		profileList = ProfileDB.getAllProfiles();
		userList= UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

		sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);

		distributePublicKeys(publicKey);
		
		return SUCCESS;
	}

	@Action(value = "/manage/disablePublicKey",
			results = {
					@Result(name = "success", location = "/manage/view_keys.jsp")
			}
	)
	public String disablePublicKey() {
		
		publicKey= PublicKeyDB.getPublicKey(publicKey.getId());

		PublicKeyDB.disableKey(publicKey.getId());

		profileList = ProfileDB.getAllProfiles();
		userList= UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

		sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);

		distributePublicKeys(publicKey);
		
		return SUCCESS;
	}

	@Action(value = "/manage/viewKeys",
			results = {
					@Result(name = "success", location = "/manage/view_keys.jsp")
			}
	)
	public String manageViewKeys() {

		profileList = ProfileDB.getAllProfiles();
		userList= UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

		sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);

		return SUCCESS;
	}

	

	@Action(value = "/admin/viewKeys",
			results = {
					@Result(name = "success", location = "/admin/view_keys.jsp")
			}
	)
	public String adminViewKeys() {

		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		String userType = AuthUtil.getUserType(servletRequest.getSession());

		if (Auth.MANAGER.equals(userType)) {
			profileList = ProfileDB.getAllProfiles();
		} else {
			profileList = UserProfileDB.getProfilesByUser(userId);
		}
		sortedSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);
		
		userPublicKeyList = PublicKeyDB.getUniquePublicKeysForUser(userId);

		return SUCCESS;
	}
	
	@Action(value = "/admin/savePublicKey",
			results = {
					@Result(name = "input", location = "/admin/view_keys.jsp"),
					@Result(name = "success", location = "/admin/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}&keyNm=${publicKey.keyNm}", type = "redirect")
			}
	)
	public String savePublicKeys() {

		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		String userType = AuthUtil.getUserType(servletRequest.getSession());

		publicKey.setUserId(userId);


		if (Auth.MANAGER.equals(userType) || UserProfileDB.checkIsUsersProfile(userId, publicKey.getProfile().getId())) {
			if (publicKey.getId() != null) {
				PublicKeyDB.updatePublicKey(publicKey);
			} else {
				PublicKeyDB.insertPublicKey(publicKey);
			}
			
			distributePublicKeys(publicKey);
		}

		return SUCCESS;
	}

	@Action(value = "/admin/deletePublicKey",
			results = {
					@Result(name = "input", location = "/admin/view_keys.jsp"),
					@Result(name = "success", location = "/admin/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
			}
	)
	public String deletePublicKey() {


		if (publicKey.getId() != null) {
			//get public key then delete
			publicKey = PublicKeyDB.getPublicKey(publicKey.getId());
			PublicKeyDB.deletePublicKey(publicKey.getId(), AuthUtil.getUserId(servletRequest.getSession()));
		}

		distributePublicKeys(publicKey);

		return SUCCESS;
	}

	public static final String PVT_KEY="privateKey";
	
	@Action(value = "/admin/downloadPvtKey")
	public String downloadPvtKey() {
		
		String privateKey=EncryptionUtil.decrypt((String)servletRequest.getSession().getAttribute(PVT_KEY));

		if(StringUtils.isNotEmpty(publicKey.getKeyNm()) && StringUtils.isNotEmpty(privateKey)) {
			try {

				servletResponse.setContentType("application/octet-stream");
				servletResponse.setHeader("Content-Disposition", "attachment;filename=" + publicKey.getKeyNm() + ".key");
				servletResponse.getOutputStream().write(privateKey.getBytes());
				servletResponse.getOutputStream().flush();
				servletResponse.getOutputStream().close();
			} catch (Exception ex) {
                log.error(ex.toString(), ex);
			}
		}
		//remove pvt key
		servletRequest.getSession().setAttribute(PVT_KEY, null);
		servletRequest.getSession().removeAttribute(PVT_KEY);

		return null;
	}

	/**
	 * generates public private key from passphrase
	 *  
	 * @param username username to set in public key comment
	 * @param keyname keyname to set in public key comment
	 * @return public key
	 */
	public String generateUserKey(String username, String keyname) {

		//set key type
		int type = KeyPair.RSA;
		if(SSHUtil.KEY_TYPE.equals("dsa")) {
			type = KeyPair.DSA;
		} else if(SSHUtil.KEY_TYPE.equals("ecdsa")) {
			type = KeyPair.ECDSA;
		}

		JSch jsch = new JSch();
				
		String pubKey=null;
		try {

			KeyPair keyPair = KeyPair.genKeyPair(jsch, type, SSHUtil.KEY_LENGTH);

			OutputStream os = new ByteArrayOutputStream();
			keyPair.writePrivateKey(os, publicKey.getPassphrase().getBytes());
			//set private key
			servletRequest.getSession().setAttribute(PVT_KEY, EncryptionUtil.encrypt(os.toString()));
			
			os = new ByteArrayOutputStream();
			keyPair.writePublicKey(os, username + "@" + keyname);
			pubKey = os.toString();


			
			
			keyPair.dispose();
		} catch (Exception ex) {
            log.error(ex.toString(), ex);
		}

		return pubKey;
	}

	/**
	 * Validates all fields for adding a public key
	 */
	public void validateSavePublicKeys() {

		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		
		if (publicKey == null
				|| publicKey.getKeyNm() == null
				|| publicKey.getKeyNm().trim().equals("")) {
			addFieldError("publicKey.keyNm", "Required");
			
		} 
		
		if(publicKey!=null) {
			
			if(existingKeyId!=null){
				
				publicKey.setPublicKey(PublicKeyDB.getPublicKey(existingKeyId).getPublicKey());
				
			} else if("true".equals(AppConfig.getProperty("forceUserKeyGeneration"))){
				
				if (publicKey.getPassphrase() == null ||
						publicKey.getPassphrase().trim().equals("")) {
					addFieldError("publicKey.passphrase", "Required");
				}
				else if (publicKey.getPassphraseConfirm() == null ||
						publicKey.getPassphraseConfirm().trim().equals("")) {
					addFieldError("publicKey.passphraseConfirm", "Required");
				}
				else if(!publicKey.getPassphrase().equals(publicKey.getPassphraseConfirm())) {
					addActionError("Passphrases do not match");
				}
				else if(!PasswordUtil.isValid(publicKey.getPassphrase())){
					addActionError(PasswordUtil.PASSWORD_REQ_ERROR_MSG);
				}
				else {
					publicKey.setPublicKey(generateUserKey(UserDB.getUser(userId).getUsername(), publicKey.getKeyNm()));
				}
			}
			
			if( publicKey.getPublicKey() == null || publicKey.getPublicKey().trim().equals("")) {
				addFieldError("publicKey.publicKey", "Required");
			
			} else if (SSHUtil.getFingerprint(publicKey.getPublicKey()) == null || SSHUtil.getKeyType(publicKey.getPublicKey()) == null) {
				addFieldError("publicKey.publicKey", "Invalid");

			} else if (PublicKeyDB.isKeyDisabled(SSHUtil.getFingerprint(publicKey.getPublicKey()))) {
				addActionError("This key has been disabled. Please generate and set a new public key.");
				addFieldError("publicKey.publicKey", "Invalid");

			} else if (PublicKeyDB.isKeyRegistered(userId, publicKey)) {
				addActionError("This key has already been registered under selected profile.");
				addFieldError("publicKey.publicKey", "Invalid");

			}
		}

		if (!this.getFieldErrors().isEmpty()) {

			String userType = AuthUtil.getUserType(servletRequest.getSession());

			if (Auth.MANAGER.equals(userType)) {
				profileList = ProfileDB.getAllProfiles();
			} else {
				profileList = UserProfileDB.getProfilesByUser(userId);
			}

			sortedSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);
			userPublicKeyList = PublicKeyDB.getUniquePublicKeysForUser(userId);
		}

	}



	/**
	 * distribute public keys to all systems or to profile
	 *
	 * @param publicKey public key to distribute
	 */
	private void distributePublicKeys(PublicKey publicKey){
		
		if (publicKey.getProfile() != null && publicKey.getProfile().getId() != null) {
			RefreshAuthKeyUtil.refreshProfileSystems(publicKey.getProfile().getId());
		} else {
			RefreshAuthKeyUtil.refreshAllSystems();
		}
		
	}


	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public List<Profile> getProfileList() {
		return profileList;
	}

	public void setProfileList(List<Profile> profileList) {
		this.profileList = profileList;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public SortedSet getSortedSet() {
		return sortedSet;
	}

	public void setSortedSet(SortedSet sortedSet) {
		this.sortedSet = sortedSet;
	}

	public List<Long> getSystemSelectId() {
		return systemSelectId;
	}

	public void setSystemSelectId(List<Long> systemSelectId) {
		this.systemSelectId = systemSelectId;
	}

	public HostSystem getPendingSystem() {
		return pendingSystem;
	}

	public void setPendingSystem(HostSystem pendingSystem) {
		this.pendingSystem = pendingSystem;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public HostSystem getHostSystem() {
		return hostSystem;
	}

	public void setHostSystem(HostSystem hostSystem) {
		this.hostSystem = hostSystem;
	}

	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}

	public void setServletResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

	public boolean getForceUserKeyGenEnabled() {
		return forceUserKeyGenEnabled;
	}

	public void setForceUserKeyGenEnabled(boolean forceUserKeyGenEnabled) {
		this.forceUserKeyGenEnabled = forceUserKeyGenEnabled;
	}

	public List<PublicKey> getUserPublicKeyList() {
		return userPublicKeyList;
	}

	public void setUserPublicKeyList(List<PublicKey> userPublicKeyList) {
		this.userPublicKeyList = userPublicKeyList;
	}

	public Long getExistingKeyId() {
		return existingKeyId;
	}

	public void setExistingKeyId(Long existingKeyId) {
		this.existingKeyId = existingKeyId;
	}
}
