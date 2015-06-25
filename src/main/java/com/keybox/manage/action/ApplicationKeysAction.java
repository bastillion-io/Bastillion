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


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.google.gson.Gson;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.model.*;
import com.keybox.manage.util.AWSClientConfig;
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
import java.util.Map;

/**
 * Action to generate and distribute System and EC2 keys
 */
@SuppressWarnings("unchecked")
public class ApplicationKeysAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	/**
	 * INFO:
	 * here are the applicationKey the Initial System Key for KeyBoxes "Add System" - Systems
	 * EC2Key, are the Keys for Amazon EC2 Systems 
	 */

	HttpServletRequest servletRequest;
	HttpServletResponse servletResponse;
	ApplicationKey applicationKey;
	ApplicationKey ec2Key;
	SortedSet sortedSet = new SortedSet();
	SortedSet sortedEC2Set = new SortedSet();
	List<Long> systemSelectId;
	File appKeyFile;
	File ec2KeyFile;
	
	static Map<String, String> ec2RegionMap = AppConfig.getMapProperties("ec2Regions");
	List<AWSCred> awsCredList = AWSCredDB.getvalidAWSCredList();
	
	Long existingKeyId;

	/**
	 * Show (KeyBox)Initial System Keys and EC2 Keys
	 * @return
	 */
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
		if(sortedEC2Set.getFilterMap().get(PrivateKeyDB.FILTER_BY_ENABLED)==null){
			sortedEC2Set.getFilterMap().put(PrivateKeyDB.FILTER_BY_ENABLED, "true");
		}
		
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
		sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
		
		return SUCCESS;
	}

	/**
     * returns keypairs as a json string
     */
    @Action(value = "/manage/getKeyPairJSON"
    )
    public String getKeyPairJSON() {

        AWSCred awsCred = AWSCredDB.getAWSCred(ec2Key.getAwsCredentials().getId());

        //set  AWS credentials for service
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
        AmazonEC2 service = new AmazonEC2Client(awsCredentials, AWSClientConfig.getClientConfig());

        service.setEndpoint(ec2Key.getEc2Region());

        DescribeKeyPairsRequest describeKeyPairsRequest = new DescribeKeyPairsRequest();

        DescribeKeyPairsResult describeKeyPairsResult = service.describeKeyPairs(describeKeyPairsRequest);

        List<KeyPairInfo> keyPairInfoList = describeKeyPairsResult.getKeyPairs();
        String json = new Gson().toJson(keyPairInfoList);
        try {
            servletResponse.getOutputStream().write(json.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
	

	/**
	 * Save (KeyBox)Initial System Key
	 * @return
	 */
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
	
	/**
	 * Save EC2 Key
	 * @return
	 */
	@Action(value = "/manage/saveEC2Key",
			results = {
					@Result(name = "input", location = "/manage/view_application_keys.jsp"),
					@Result(name = "success", location = "/manage/ViewApplicationKeys.action", type = "redirect")
					//@Result(name = "success", location = "/admin/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}&keyNm=${publicKey.keyNm}", type = "redirect")
			}
	)
	public String saveEC2Key() {
		PrivateKeyDB.insertApplicationKey(ec2Key);
		return SUCCESS;
	}
	
	/**
	 * Enable Key
	 * @return
	 */
	@Action(value = "/manage/enableApplicationKey",
		results = {
				@Result(name = "success", location = "/manage/view_application_keys.jsp")
		}
	)
	public String enableApplicationKey() {
	
		applicationKey= PrivateKeyDB.getApplicationKeyByID(applicationKey.getId());
	
		PrivateKeyDB.enableApplicationKey(applicationKey.getId());
		
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
		sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
	
		return SUCCESS;
	}
	
	/**
	 * Disable Key
	 * @return
	 */
	@Action(value = "/manage/disableApplicationKey",
		results = {
				@Result(name = "success", location = "/manage/view_application_keys.jsp")
		}
	)
	public String disableApplicationKey() {
	
		applicationKey= PrivateKeyDB.getApplicationKeyByID(applicationKey.getId());
	
		PrivateKeyDB.disableApplicationKey(applicationKey.getId());
	
		sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
		sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
	
		return SUCCESS;
	}
	
	/**
	 * Delete (KeyBox)Initial System Key
	 * @return
	 */
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
        sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
        
        return SUCCESS;
    }
	
	/**
	 * Delete EC2 Key
	 * @return
	 */
	@Action(value = "/manage/deleteEC2Key",
            results = {
			@Result(name = "success", location = "/manage/view_application_keys.jsp")
            }
    )
    public String deleteEC2Key() {

		if (ec2Key.getId() != null) {
            PrivateKeyDB.deleteEC2Key(ec2Key.getId());
        }
        sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
        sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
        
        return SUCCESS;
    }
	
	/**
	 * Validates all fields for adding a (KeyBox)Initial System Key
	 */
	public void validateSaveApplicationKeys() {
		
		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		
		if(applicationKey.getKeyname() == null ||
				applicationKey.getKeyname().trim().equals("")){
			addFieldError("applicationKey.keyname", "Keyname not set");
		} else if(PrivateKeyDB.keyNameExistsInRegion(applicationKey.getKeyname(),applicationKey.getEc2Region())){
			addFieldError("applicationKey.keyname", "KeyName has already been set.");
		} else if(!applicationKey.getPassphrase().equals(applicationKey.getPassphraseConfirm())) {
			addActionError("Passphrases do not match");
		} else{
			try {
	        	JSch jsch = new JSch();
	        	
				KeyPair keyPair = KeyPair.load(jsch, appKeyFile.getAbsolutePath());
				
				if(keyPair.getFingerPrint()==null){
					addActionError("Please upload the file without integrated passphrase");
					addFieldError("appKeyFile", "File containing passphrase");
				
				}else if(FingerprintDB.isFingerprintExistsInRegion(keyPair.getFingerPrint(),applicationKey.getEc2Region())){
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
			sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
		}
	}
	
	/**
	 * Validates all fields for adding a EC2 Key
	 */
	public void validateSaveEC2Key() {
		Long userId = AuthUtil.getUserId(servletRequest.getSession());
		
		if(ec2Key.getKeyname() == null ||
				ec2Key.getKeyname().trim().equals("")){
			addFieldError("ec2Key.keyname", "Keyname not set");
		} else if(PrivateKeyDB.keyNameExistsInRegion(ec2Key.getKeyname(),ec2Key.getEc2Region())){
			addActionError("Key has already been set.");
			addFieldError("ec2Key.keyname", "Key has already been set.");
		} else{
			try {
	        	JSch jsch = new JSch();
				KeyPair keyPair = KeyPair.load(jsch, ec2KeyFile.getAbsolutePath());
				
				if(keyPair.getFingerPrint()==null){
					addActionError("Please upload the file without integrated passphrase.");
					addFieldError("ec2KeyFile", "File containing passphrase.");
				}else if(FingerprintDB.isFingerprintExistsInRegion(keyPair.getFingerPrint(),ec2Key.getEc2Region())){
					addActionError("Key already exists for Region.");
					addFieldError("ec2KeyFile", "Key already exists.");
				}else{
					ec2Key.setInitialkey(true);
					ec2Key.setFingerprint(new Fingerprint(keyPair.getFingerPrint()));
					
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					
					ec2Key.setPassphrase(null);
					keyPair.writePrivateKey(out);
				
					String privateKey = out.toString();
					ec2Key.setPrivateKey(privateKey);
					
					out = new ByteArrayOutputStream();
					keyPair.writePublicKey(out, keyPair.getPublicKeyComment());
					String publicKey = out.toString();
					ec2Key.setPublicKey(publicKey);
					
					ec2Key.setType(SSHUtil.getKeyType(publicKey));
					ec2Key.setUserId(userId);
					ec2Key.setEnabled(true);
					
				}
			} catch (Exception e) {
				addActionError("Private Key does not readable");
				addFieldError("ec2KeyFile", "Private Key does not readable");
				e.printStackTrace();
			}
		}
		if (!this.getFieldErrors().isEmpty()) {
			sortedSet = PrivateKeyDB.getApplicationKeySet(sortedSet);
			sortedEC2Set = PrivateKeyDB.getEC2KeySet(sortedEC2Set);
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

	public static Map<String, String> getEc2RegionMap() {
		return ec2RegionMap;
	}

	public static void setEc2RegionMap(Map<String, String> ec2RegionMap) {
		ApplicationKeysAction.ec2RegionMap = ec2RegionMap;
	}

	public List<AWSCred> getAwsCredList() {
		return awsCredList;
	}

	public void setAwsCredList(List<AWSCred> awsCredList) {
		this.awsCredList = awsCredList;
	}

	public Long getExistingKeyId() {
		return existingKeyId;
	}

	public void setExistingKeyId(Long existingKeyId) {
		this.existingKeyId = existingKeyId;
	}

	public SortedSet getSortedEC2Set() {
		return sortedEC2Set;
	}

	public void setSortedEC2Set(SortedSet sortedEC2Set) {
		this.sortedEC2Set = sortedEC2Set;
	}

	public ApplicationKey getEc2Key() {
		return ec2Key;
	}

	public void setEc2Key(ApplicationKey ec2Key) {
		this.ec2Key = ec2Key;
	}

	public File getEc2KeyFile() {
		return ec2KeyFile;
	}

	public void setEc2KeyFile(File ec2KeyFile) {
		this.ec2KeyFile = ec2KeyFile;
	}

}
