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

import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.SchSession;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadAndPushAction extends ActionSupport {


    File upload;
    String uploadContentType;
    String uploadFileName;
    List<Long> idList = new ArrayList<Long>();
    String pushDir = "~";
    List<SystemStatus> systemStatusList;
    SystemStatus pendingSystemStatus;
    SystemStatus currentSystemStatus;

    public static String UPLOAD_PATH = DBUtils.class.getClassLoader().getResource(".").getPath() + "../upload";


    @Action(value = "/manage/setUpload",
            results = {
                    @Result(name = "success", location = "/manage/upload.jsp")
            }
    )
    public String setUpload() throws Exception {
        SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForSystems(idList));
        return SUCCESS;

    }


    @Action(value = "/manage/upload",
            results = {
                    @Result(name = "input", location = "/manage/upload.jsp"),
                    @Result(name = "success", location = "/manage/upload_result.jsp")
            }
    )
    public String upload() {


        try {
            File destination = new File(UPLOAD_PATH, uploadFileName);
            FileUtils.copyFile(upload, destination);


            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();

            systemStatusList = SystemStatusDB.getAllSystemStatus();


        } catch (Exception e) {
            e.printStackTrace();
            return INPUT;
        }

        return SUCCESS;
    }

    @Action(value = "/manage/push",
            results = {
                    @Result(name = "success", location = "/manage/upload_result.jsp")
            }
    )
    public String push() {


        try {

            //get next pending system
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
            if (pendingSystemStatus != null) {
                //get session for system
                SchSession session = SecureShellAction.getSchSessionMap().get(pendingSystemStatus.getId());
                //push upload to system
                currentSystemStatus = SSHUtil.pushUpload(pendingSystemStatus, session.getSession(), UPLOAD_PATH + "/" + uploadFileName, pushDir + "/" + uploadFileName);

                //update system status
                SystemStatusDB.updateSystemStatus(currentSystemStatus);

                pendingSystemStatus = SystemStatusDB.getNextPendingSystem();

            }

            //if push has finished to all servers then delete uploaded file
            if (pendingSystemStatus == null) {
                File delFile = new File(UPLOAD_PATH, uploadFileName);
                FileUtils.deleteQuietly(delFile);

            }
            systemStatusList = SystemStatusDB.getAllSystemStatus();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * Validates all fields for uploading a file
     */
    public void validateUpload() {

        if (uploadFileName == null || uploadFileName.trim().equals("")) {
            addFieldError("upload", "Required");

        }
        if (pushDir == null || pushDir.trim().equals("")) {
            addFieldError("pushPath", "Required");

        }

    }

    public File getUpload() {
        return upload;
    }

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public String getUploadContentType() {
        return uploadContentType;
    }

    public void setUploadContentType(String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getPushDir() {
        return pushDir;
    }

    public void setPushDir(String pushDir) {
        this.pushDir = pushDir;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }

    public List<SystemStatus> getSystemStatusList() {
        return systemStatusList;
    }

    public void setSystemStatusList(List<SystemStatus> systemStatusList) {
        this.systemStatusList = systemStatusList;
    }

    public SystemStatus getPendingSystemStatus() {
        return pendingSystemStatus;
    }

    public void setPendingSystemStatus(SystemStatus pendingSystemStatus) {
        this.pendingSystemStatus = pendingSystemStatus;
    }

    public SystemStatus getCurrentSystemStatus() {
        return currentSystemStatus;
    }

    public void setCurrentSystemStatus(SystemStatus currentSystemStatus) {
        this.currentSystemStatus = currentSystemStatus;
    }
}
