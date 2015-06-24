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

import com.keybox.manage.db.AWSCredDB;
import com.keybox.manage.model.AWSCred;
import com.keybox.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

/**
 * Action to set aws credentials
 * adapted from EC2Box
 */
public class AWSCredAction extends ActionSupport {

    AWSCred awsCred;
    SortedSet sortedSet= new SortedSet();

    /**
     * Show AWS Credentials list
     * @return
     */
    @Action(value = "/manage/viewAWSCred",
            results = {
                    @Result(name = "success", location = "/manage/view_aws_cred.jsp")
            }
    )
    public String viewAWSCred() {
        sortedSet = AWSCredDB.getAWSCredSet(sortedSet);
        return SUCCESS;
    }

    /**
     * Save AWS Credential
     * @return
     */
    @Action(value = "/manage/saveAWSCred",
            results = {
                    @Result(name = "success", location = "/manage/viewAWSCred.action", type="redirect"),
                    @Result(name = "input", location = "/manage/view_aws_cred.jsp")
            }
    )
    public String saveAWSCred() {
        AWSCredDB.saveAWSCred(awsCred);
        return SUCCESS;
    }


    /**
     * Delete AWS Credential
     * @return
     */
    @Action(value = "/manage/deleteAWSCred",
            results = {
                    @Result(name = "success", location = "/manage/viewAWSCred.action", type="redirect")
            }
    )
    public String deleteAWSCred() {
        AWSCredDB.deleteAWSCred(awsCred.getId());
        return SUCCESS;
    }

    /**
     * Validates fields for credential submit
     */
    public void validateSaveAWSCred() {
        if (awsCred.getAccessKey() == null ||
                awsCred.getAccessKey().trim().equals("")) {
            addFieldError("awsCred.accessKey", "Required");
        }
        if (awsCred.getSecretKey() == null ||
                awsCred.getSecretKey().trim().equals("")) {
            addFieldError("awsCred.secretKey", "Required");
        }
        if (!this.hasErrors()) {
            if (awsCred.isValid()==false){
            	addActionError("Invalid Credentials");
            }
        }
        if(this.hasActionErrors() || this.hasErrors()){
            sortedSet = AWSCredDB.getAWSCredSet(sortedSet);
        }
    }


    public AWSCred getAwsCred() {
        return awsCred;
    }

    public void setAwsCred(AWSCred awsCred) {
        this.awsCred = awsCred;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }
}
