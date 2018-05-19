/**
 *    Copyright (C) 2013 Loophole, LLC
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
package com.keybox.manage.control;

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SchSession;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SSHUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;
import loophole.mvc.filter.SecurityFilter;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class UploadAndPushKtrl extends BaseKontroller {

    public static final String UPLOAD_PATH = DBUtils.class.getClassLoader().getResource(".").getPath() + "../upload";
    private static Logger log = LoggerFactory.getLogger(UploadAndPushKtrl.class);

    @Model(name = "upload")
    File upload;
    @Model(name = "uploadFileName")
    String uploadFileName;
    @Model(name = "idList")
    List<Long> idList = new ArrayList<>();
    @Model(name = "pushDir")
    String pushDir = "~";
    @Model(name = "hostSystemList")
    List<HostSystem> hostSystemList;
    @Model(name = "pendingSystemStatus")
    HostSystem pendingSystemStatus;
    @Model(name = "currentSystemStatus")
    HostSystem currentSystemStatus;


    public UploadAndPushKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/setUpload", method = MethodType.GET)
    public String setUpload() throws Exception {
        Long userId = AuthUtil.getUserId(getRequest().getSession());

        SystemStatusDB.setInitialSystemStatus(idList, userId, AuthUtil.getUserType(getRequest().getSession()));

        return "/admin/upload.html";

    }


    @Kontrol(path = "/admin/uploadSubmit", method = MethodType.POST)
    public String uploadSubmit() {

        String retVal = "/admin/upload_result.html";
        Long userId = AuthUtil.getUserId(getRequest().getSession());
        try {

            List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(getRequest());
            for (FileItem item : multiparts) {
                if (!item.isFormField()) {
                    uploadFileName = new File(item.getName()).getName();
                    File path = new File(UPLOAD_PATH);
                    if(!path.exists()) {
                        path.mkdirs();
                    }
                    upload = new File(UPLOAD_PATH + File.separator + uploadFileName);
                    item.write(upload);
                } else {
                    pushDir =  item.getString();
                }
            }

            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

            hostSystemList = SystemStatusDB.getAllSystemStatus(userId);


        } catch (Exception e) {
            log.error(e.toString(), e);
            retVal = "/admin/upload.html";
        }
        //reset csrf token back since it's already set on page load
        getRequest().getSession().setAttribute(SecurityFilter._CSRF,
                getRequest().getParameter(SecurityFilter._CSRF));

        return retVal;
    }

    @Kontrol(path = "/admin/push", method = MethodType.POST)
    public String push() {

        Long userId = AuthUtil.getUserId(getRequest().getSession());
        Long sessionId = AuthUtil.getSessionId(getRequest().getSession());
        try {

            //get next pending system
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
            if (pendingSystemStatus != null) {
                //get session for system
                SchSession session = null;
                for (Integer instanceId : SecureShellKtrl.getUserSchSessionMap().get(sessionId).getSchSessionMap().keySet()) {

                    //if host system id matches pending system then upload
                    if (pendingSystemStatus.getId().equals(SecureShellKtrl.getUserSchSessionMap().get(sessionId).getSchSessionMap().get(instanceId).getHostSystem().getId())) {
                        session = SecureShellKtrl.getUserSchSessionMap().get(sessionId).getSchSessionMap().get(instanceId);
                    }
                }

                if (session != null) {

                    //push upload to system
                    currentSystemStatus = SSHUtil.pushUpload(pendingSystemStatus, session.getSession(), UPLOAD_PATH + "/" + uploadFileName, pushDir + "/" + uploadFileName);

                    //update system status
                    SystemStatusDB.updateSystemStatus(currentSystemStatus, userId);

                    pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
                }

            }

            //if push has finished to all servers then delete uploaded file
            if (pendingSystemStatus == null) {
                File delFile = new File(UPLOAD_PATH, uploadFileName);
                FileUtils.deleteQuietly(delFile);


                //delete all expired files in upload path
                File delDir = new File(UPLOAD_PATH);
                if (delDir.isDirectory()) {

                    //set expire time to delete all files older than 48 hrs
                    Calendar expireTime = Calendar.getInstance();
                    expireTime.add(Calendar.HOUR, -48);

                    Iterator<File> filesToDelete = FileUtils.iterateFiles(delDir, new AgeFileFilter(expireTime.getTime()), TrueFileFilter.TRUE);
                    while (filesToDelete.hasNext()) {
                        delFile = filesToDelete.next();
                        delFile.delete();
                    }

                }

            }
            hostSystemList = SystemStatusDB.getAllSystemStatus(userId);


        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        //reset csrf token back since it's already set on page load
        getRequest().getSession().setAttribute(SecurityFilter._CSRF,
                getRequest().getParameter(SecurityFilter._CSRF));

        return "/admin/upload_result.html";
    }

}
