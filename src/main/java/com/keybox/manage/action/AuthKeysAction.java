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

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.model.*;
import com.keybox.manage.util.RefreshAuthKeyUtil;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
@SuppressWarnings("unchecked")
public class AuthKeysAction extends ActionSupport implements ServletRequestAware {


	HttpServletRequest servletRequest;
	List<Profile> profileList;
	List<User> userList;
	PublicKey publicKey;
	SortedSet sortedSet = new SortedSet();
	List<Long> systemSelectId;

	HostSystem pendingSystem = null;
	HostSystem hostSystem = new HostSystem();


	
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

		return SUCCESS;
	}
	
	@Action(value = "/admin/savePublicKey",
			results = {
					@Result(name = "input", location = "/admin/view_keys.jsp"),
					@Result(name = "success", location = "/admin/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
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

	/**
	 * Validates all fields for adding a public key
	 */
	public void validateSavePublicKeys() {
            
                boolean bKeyIsDuplicate = false;
                Long userId = AuthUtil.getUserId(servletRequest.getSession());
                SortedSet sortedTempSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);
                
                try {
                    
                    List<PublicKey> items = sortedTempSet.getItemList();

                    //Iterate through the keys untill you find the mathcing key, then break and flag.
                    for (PublicKey k : items) {
                        if (k.getFingerprint().equals(SSHUtil.getFingerprint(publicKey.getPublicKey()))) {
                            bKeyIsDuplicate = true;
                            break;
                        }
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            
            
		if (publicKey == null
				|| publicKey.getKeyNm() == null
				|| publicKey.getKeyNm().trim().equals("")) {
			addFieldError("publicKey.keyNm", "Required");
		}
		if (publicKey == null
				|| publicKey.getPublicKey() == null
				|| publicKey.getPublicKey().trim().equals("")) {
			addFieldError("publicKey.publicKey", "Required");
		}
		else if(SSHUtil.getFingerprint(publicKey.getPublicKey()) == null
				|| SSHUtil.getKeyType(publicKey.getPublicKey()) == null) {
			addFieldError("publicKey.publicKey", "Invalid");
		} else if(PublicKeyDB.isKeyDisabled(SSHUtil.getFingerprint(publicKey.getPublicKey()))){
			addActionError("This key has been disabled. Please generate and set a new public key.");
			addFieldError("publicKey.publicKey", "Invalid");
		} else if (bKeyIsDuplicate) {
                        addActionError("This key already exists in the key database. Please check this key and avoid duplicates!");
			addFieldError("publicKey.publicKey", "Invalid");
                }
                
                
		if (!this.getFieldErrors().isEmpty()) {
			profileList = UserProfileDB.getProfilesByUser(userId);
			sortedSet = PublicKeyDB.getPublicKeySet(sortedSet, userId);
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
}
