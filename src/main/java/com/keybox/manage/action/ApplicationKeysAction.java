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
package com.keybox.manage.action;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.model.*;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
@SuppressWarnings("unchecked")
public class ApplicationKeysAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {


	HttpServletRequest servletRequest;
	HttpServletResponse servletResponse;
	ApplicationKey applicationKey;
	SortedSet sortedSet = new SortedSet();
	List<Long> systemSelectId;
	File appKeyFile;
	
	//boolean forceUserKeyGenEnabled="true".equals(AppConfig.getProperty("forceUserKeyGeneration"));
	

	@Action(value = "/manage/ViewApplicationKeys",
			results = {
					@Result(name = "success", location = "/manage/view_application_keys.jsp")
			}
	)
	public String manageViewApplicationKeys() {

		//if null default to true
		if(sortedSet.getFilterMap().get(PrivateKeyDB.FILTER_BY_ENABLED)==null){
			sortedSet.getFilterMap().put(PrivateKeyDB.FILTER_BY_ENABLED, "true");
		}
		
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
		
		return SUCCESS;
	}

	

	
	@Action(value = "/manage/saveApplicationKey",
			results = {
					@Result(name = "input", location = "/manage/view_application_keys.jsp"),
					@Result(name = "success", location = "/manage/ViewApplicationKeys.action", type = "redirect")
					//@Result(name = "success", location = "/admin/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}&keyNm=${publicKey.keyNm}", type = "redirect")
			}
	)
	public String saveApplicationKeys() {
		PrivateKeyDB.insertApplicationKey(applicationKey);
		return SUCCESS;
	}
	
	@Action(value = "/manage/enableApplicationKey",
		results = {
				@Result(name = "success", location = "/manage/view_application_keys.jsp")
		}
	)
	public String enableApplicationKey() {
	
		applicationKey= PrivateKeyDB.getApplicationKeyByID(applicationKey.getId());
	
		PrivateKeyDB.enableApplicationKey(applicationKey.getId());
		
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
	
		return SUCCESS;
	}
	
	@Action(value = "/manage/disableApplicationKey",
		results = {
				@Result(name = "success", location = "/manage/view_application_keys.jsp")
		}
	)
	public String disableApplicationKey() {
	
		applicationKey= PrivateKeyDB.getApplicationKeyByID(applicationKey.getId());
	
		PrivateKeyDB.disableApplicationKey(applicationKey.getId());
	
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
	
		return SUCCESS;
	}
	
	@Action(value = "/manage/deleteApplicationKey",
            results = {
			@Result(name = "success", location = "/manage/view_application_keys.jsp")
            }
    )
    public String deleteApplicationKey() {

		if (applicationKey.getId() != null) {
            PrivateKeyDB.deleteApplicationKey(applicationKey.getId());
        }
        sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
        
        return SUCCESS;
    }
	
	/**
	 * Validates all fields for adding a public key
	 */
	public void validateSaveApplicationKeys() {
		
		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		
		if(applicationKey.getKeyname() == null ||
				applicationKey.getKeyname().trim().equals("")){
			addFieldError("applicationKey.keyname", "Keyname not set");
		} else if(!applicationKey.getPassphrase().equals(applicationKey.getPassphraseConfirm())) {
			addActionError("Passphrases do not match");
		} else{
			try {
	        	JSch jsch = new JSch();
	        	
				KeyPair keyPair = KeyPair.load(jsch, appKeyFile.getAbsolutePath());
				
				if(keyPair.getFingerPrint()==null){
					addActionError("Please upload the file without integrated passphrase");
					addFieldError("appKeyFile", "File containing passphrase");
				
				}else if(FingerprintDB.isFingerprintExists(keyPair.getFingerPrint())){
					addActionError("Key already exists");
					addFieldError("appKeyFile", "Key already exists");
				}else{
					applicationKey.setInitialkey(true);
					applicationKey.setFingerprint(new Fingerprint(keyPair.getFingerPrint()));
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					if(applicationKey.getPassphrase() == null || applicationKey.getPassphrase().equals(""))
					{
						applicationKey.setPassphrase(null);
						keyPair.writePrivateKey(out);
					}
					else{
						keyPair.writePrivateKey(out, applicationKey.getPassphrase().getBytes());
					}
					String privateKey = out.toString();
					applicationKey.setPrivateKey(privateKey);
					
					out = new ByteArrayOutputStream();
					keyPair.writePublicKey(out, keyPair.getPublicKeyComment());
					String publicKey = out.toString();
					applicationKey.setPublicKey(publicKey);
					
					applicationKey.setType(SSHUtil.getKeyType(publicKey));
					applicationKey.setUserId(userId);
					applicationKey.setEnabled(true);
				}
			} catch (Exception e) {
				addActionError("Private Key does not readable");
				addFieldError("appKeyFile", "Private Key does not readable");
				e.printStackTrace();
			}
		}
		if (!this.getFieldErrors().isEmpty()) {
			sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
		}
	}

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public ApplicationKey getApplicationKey() {
		return applicationKey;
	}

	public void setApplicationKey(ApplicationKey applicationKey) {
		this.applicationKey = applicationKey;
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

	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}

	public void setServletResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

	public File getAppKeyFile() {
		return appKeyFile;
	}

	public void setAppKeyFile(File appKeyFile) {
		this.appKeyFile = appKeyFile;
	}
}
