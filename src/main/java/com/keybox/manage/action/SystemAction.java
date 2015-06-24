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
import com.keybox.manage.db.AWSCredDB;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.db.ProfileSystemsDB;
import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.db.UserProfileDB;
import com.keybox.manage.model.*;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.RefreshApplicationKeyUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

/**
 * Action to manage systems
 * @author Sean Kavanagh, Robert Vorkoeper
 * 
 */
public class SystemAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	/*
	 * INFO: to understand:
	 * (KeyBoy)Systems 	--> Systems that are created by add system
	 * EC2 Systems		--> Amazons Elastic Compute Cloud, which are read by AWS credentials and EC2 Key on Amazon
	 */

	HttpServletRequest servletRequest;
	HttpServletResponse servletResponse;
    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    List<ApplicationKey> initAppList;	//List for Inital System Keys 
	Script script = null;
    String password;
    String passphrase;
    List<Profile> profileList= new ArrayList<>();
    boolean ismanager;
    boolean downloadKey = !RefreshApplicationKeyUtil.getDynamicKeyRotation();
    
    String infoAWS = "You're about to manually set up an EC2 connection."+ ""
    		+ "You rather use the EC2 settings. "
    		+ "By a manual setup Keybox is going to change the default key."
    		+ "You can't access the server with the default AWS key then. "
    		+ "Press okay to go ahead, or cancel the setup.";
    
    /**
     * Show System List for SSH Terminals and Scripts
     * Show (KeyBoy)Systems and EC2 Systems
     * @return System List
     */
    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewAdminSystems() {
    	
    	//Test AWS Credentials are valid
    	List<AWSCred> awsCredList = AWSCredDB.getAWSCredList();
    	Integer awsCredErrorCount = 0;
    	for (AWSCred awsCred : awsCredList) {
    		if(!awsCred.isValid()) 
    			{ awsCredErrorCount++; }
    	}
    	if(awsCredErrorCount>0)
    	{
    		if(awsCredList.size()==1){
    			addActionError("The AWS Credential is invalid");
    		}else{
    			addActionError(awsCredErrorCount + " of " + awsCredList.size() + " AWS Credentials are invalid");
    		}
    	}
    	
    	ProfileSystemsDB.updateProfileAWSSysteme();
    	
        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        ismanager = Auth.MANAGER.equals(AuthUtil.getUserType(servletRequest.getSession()));
        if (ismanager) {
            sortedSet = SystemDB.getAdminSystemSet(sortedSet, userId);
            profileList=ProfileDB.getAllProfiles();
        } else {
            sortedSet = SystemDB.getUserSystemSet(sortedSet, userId);
            profileList= UserProfileDB.getProfilesByUser(userId);
        }
        if (script != null && script.getId() != null) {
            script = ScriptDB.getScript(script.getId(), userId);
        }
        return SUCCESS;
    }

    /**
     * Show System List for Manage Systems
     * Show (KeyBoy)Systems (without EC2 Systems)
     * @return Manage System List
     */
    @Action(value = "/manage/viewSystems",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String viewManageSystems() {

    	initAppList = PrivateKeyDB.getInitialApplicationKey();
    	sortedSet.getFilterMap().put("region", "---"); //Filter for no EC2 Systems
        sortedSet = SystemDB.getSystemSet(sortedSet); 
        return SUCCESS;
    }

    /**
     * Test and Save (KeyBoy)Systems
     * On Test Change System Key, if dynamic Key enabled
     * @return 
     */
    @Action(value = "/manage/saveSystem",
            results = {
                    @Result(name = "input", location = "/manage/view_systems.jsp"),
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String saveSystem() {
        String retVal=SUCCESS;
        initAppList = PrivateKeyDB.getInitialApplicationKey();

        if(hostSystem.getApplicationKey().getId() != null)
        {
        	hostSystem.setApplicationKey(PrivateKeyDB.getApplicationKeyByID(hostSystem.getApplicationKey().getId()));
        	boolean newAppKey = false;
        	if(SSHUtil.dynamicKeys && hostSystem.getApplicationKey().isInitialkey() && hostSystem.getInstance().equals("---"))
			{
				newAppKey = true;
			}
	        hostSystem = SSHUtil.authAndAddPubKey(hostSystem, passphrase, password, newAppKey);
        } else {
        	hostSystem.setStatusCd(HostSystem.PRIVAT_KEY_FAIL_STATUS);
        }

        if (hostSystem.getId() != null) {
            SystemDB.updateSystem(hostSystem);
        } else {
            hostSystem.setId(SystemDB.insertSystem(hostSystem));
        }
        sortedSet.getFilterMap().put("region", "---");
        sortedSet = SystemDB.getSystemSet(sortedSet);

        if (!HostSystem.SUCCESS_STATUS.equals(hostSystem.getStatusCd())) {
            retVal=INPUT;
        }
        return retVal;
    }
    
    /**
     * Creates a new system key for the (KeyBoy)System
     * @return
     * @author Robert Vorkoeper
     */
    @Action(value = "/manage/genNewKeyOnSystem",
            results = {
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String genNewKeyOnSystem() {
        String retVal=SUCCESS;
        initAppList = PrivateKeyDB.getInitialApplicationKey();

        hostSystem = SystemDB.getSystem(hostSystem.getId());
        if(hostSystem.getApplicationKey().getId() != null)
        {
        	hostSystem.setApplicationKey(PrivateKeyDB.getApplicationKeyByID(hostSystem.getApplicationKey().getId()));
	        hostSystem = SSHUtil.authAndAddPubKey(hostSystem, passphrase, password, true);
        } else {
        	hostSystem.setStatusCd(HostSystem.PRIVAT_KEY_FAIL_STATUS);
        }
        SystemDB.updateSystem(hostSystem);
        sortedSet.getFilterMap().put("region", "---");
        sortedSet = SystemDB.getSystemSet(sortedSet);
        return retVal;
    }

    /**
     * Remove (KeyBoy)System
     * @return
     */
    @Action(value = "/manage/deleteSystem",
            results = {
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String deleteSystem() {
        if (hostSystem.getId() != null) {
            SystemDB.deleteSystem(hostSystem.getId());
        }
        return SUCCESS;
    }
    
    /**
     * Action to Disable (KeyBoy) System
     * @return
     * @author Robert Vorkoeper
     */
    @Action(value = "/manage/disableSystem",
    		results = {
    				@Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
    	}
    )
    public String disableSystem() {
    	if (hostSystem.getId() != null) {
    		SystemDB.disableSystem(hostSystem.getId());
    	}
    	return SUCCESS;
    }
    
    /**
     * Action to Enable (KeyBoy) System
     * @return
     * @author Robert Vorkoeper
     */
    @Action(value = "/manage/enableSystem",
    		results = {
    				@Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
    	}
    )
    public String enableSystem() {
    	if (hostSystem.getId() != null) {
    		SystemDB.enableSystem(hostSystem.getId());
    	}
    	return SUCCESS;
    }
    
    /**
     * Download System Key
     * (clean-up part)
     * @return
     * @author Robert Vorkoeper
     */
    @Action(value = "/manage/downloadSystemKey",
    		results = {
    			@Result(name = "input", location = "/manage/view_systems.jsp"),
				@Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
    	}
    )
    public String downloadSystemKey() {
    	sortedSet.getFilterMap().put("region", "---");
    	sortedSet = SystemDB.getSystemSet(sortedSet);
        initAppList = PrivateKeyDB.getInitialApplicationKey();
        
    	return SUCCESS;
    }
    
    
    public static final String PVT_KEY="privateKey";
    public static final String PVT_KEY_Name="privateKeyName";
    
    /**
     * Generated Download System Key-File 
     * 
     * @return null
     * @author Robert Vorkoeper
     */
    @Action(value = "/manage/downloadPvtKey")
	public String downloadPvtKey() {
		
		String privateKey=EncryptionUtil.decrypt((String)servletRequest.getSession().getAttribute(PVT_KEY));
		String privateKeyName=(String)servletRequest.getSession().getAttribute(PVT_KEY_Name);
		
		if(StringUtils.isNotEmpty(privateKeyName) && StringUtils.isNotEmpty(privateKey)) {
			try {
				servletResponse.setContentType("application/octet-stream");
				servletResponse.setHeader("Content-Disposition", "attachment;filename=" + privateKeyName + ".key");
				servletResponse.getOutputStream().write(privateKey.getBytes());
				servletResponse.getOutputStream().flush();
				servletResponse.getOutputStream().close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//remove pvt key
		servletRequest.getSession().setAttribute(PVT_KEY, null);
		servletRequest.getSession().removeAttribute(PVT_KEY);
		servletRequest.getSession().setAttribute(PVT_KEY_Name, null);
		servletRequest.getSession().removeAttribute(PVT_KEY_Name);
		
		return null;
	}


    String testUser = "";
    String testHost = "";
    /**
     * Validates all fields for adding a host system
     */
    public void validateSaveSystem() {
        if (hostSystem == null
                || hostSystem.getDisplayNm() == null
                || hostSystem.getDisplayNm().trim().equals("")) {
            addFieldError("hostSystem.displayNm", "Required");
        }
        if (hostSystem == null
                || hostSystem.getUser() == null
                || hostSystem.getUser().trim().equals("")) {
            addFieldError("hostSystem.user", "Required");
        }
        if (hostSystem == null
                || hostSystem.getHost() == null
                || hostSystem.getHost().trim().equals("")) {
            addFieldError("hostSystem.host", "Required");
        }
        if (hostSystem == null
                || hostSystem.getPort() == null) {
            addFieldError("hostSystem.port", "Required");
        } else if (!(hostSystem.getPort() > 0)) {
            addFieldError("hostSystem.port", "Invalid");
        }

        if (hostSystem == null
                || hostSystem.getAuthorizedKeys() == null
                || hostSystem.getAuthorizedKeys().trim().equals("") || hostSystem.getAuthorizedKeys().trim().equals("~")) {
            addFieldError("hostSystem.authorizedKeys", "Required");
        }
        
        //Test whether there is a possible AWS server
    	//TODO: better test for AWS Server
        if (hostSystem.getUser().equals("ec2-user")){
        	if(!hostSystem.getUser().equals(testUser)){
        		addFieldError("hostSystem.user", "Amazon EC2 User");
        		testUser = hostSystem.getUser();
        		addActionError(infoAWS);
        	}
        }
        if (hostSystem.getHost().indexOf("compute.amazonaws.com")>-1){
        	if(!hostSystem.getHost().equals(testHost)){
        		addFieldError("hostSystem.host", "Amazon EC2 Host");
        		testHost = hostSystem.getHost();
        		if(this.getActionErrors().isEmpty()){
        			addActionError(infoAWS);
        		}
        	}
        }

        if (!this.getFieldErrors().isEmpty()) {
        	sortedSet.getFilterMap().put("region", "---");
            sortedSet = SystemDB.getSystemSet(sortedSet);
            initAppList = PrivateKeyDB.getInitialApplicationKey();
        }
    }

    /**
     * Validates Passphrase fields for download private system key
     */
    public void validateDownloadSystemKey() {
    	clearActionErrors();
    	if(!hostSystem.getApplicationKey().getPassphrase().equals(hostSystem.getApplicationKey().getPassphraseConfirm())) {
			addActionError("Passphrases do not match");
    	} else {
    		
    		String passphrase = hostSystem.getApplicationKey().getPassphrase();
        	hostSystem = SystemDB.getSystem(hostSystem.getId());
    		
        	try {
        		JSch jsch = new JSch();
        		KeyPair keyPair = KeyPair.load(jsch,hostSystem.getApplicationKey().getPrivateKey().getBytes(),hostSystem.getApplicationKey().getPublicKey().getBytes());
        		
        		ByteArrayOutputStream out = new ByteArrayOutputStream();
    			if(passphrase == null || passphrase.equals("")){
    				keyPair.writePrivateKey(out);
    			}else{
    				keyPair.writePrivateKey(out, passphrase.getBytes());
    			}
    			
    			//set private key
    			servletRequest.getSession().setAttribute(PVT_KEY, EncryptionUtil.encrypt(out.toString()));
    			//set Key Name
    			servletRequest.getSession().setAttribute(PVT_KEY_Name, hostSystem.getDisplayNm());
        	} catch (Exception ex) {
    			ex.printStackTrace();
    		}
    	}
        if (!this.getActionErrors().isEmpty()) {	
			sortedSet = SystemDB.getSystemSet(sortedSet);
            initAppList = PrivateKeyDB.getInitialApplicationKey();
            hostSystem = SystemDB.getSystem(hostSystem.getId());
    	}
	}

    public List<Profile> getProfileList() {
        return profileList;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }

    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public boolean isIsmanager() {
		return ismanager;
	}
    
    public boolean isDownloadKey() {
		return downloadKey;
	}

	public void setDownloadKey(boolean downloadKey) {
		this.downloadKey = downloadKey;
	}

	public List<ApplicationKey> getInitAppList() {
		return initAppList;
	}

	public void setInitAppList(List<ApplicationKey> initAppList) {
		this.initAppList = initAppList;
	}
	
	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}

	public void setServletResponse(HttpServletResponse servletResponse) {
		this.servletResponse = servletResponse;
	}

	public String getTestUser() {
		return testUser;
	}

	public void setTestUser(String testUser) {
		this.testUser = testUser;
	}

	public String getTestHost() {
		return testHost;
	}

	public void setTestHost(String testHost) {
		this.testHost = testHost;
	}
}
