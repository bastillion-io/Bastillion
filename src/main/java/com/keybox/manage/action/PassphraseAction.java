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

import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.util.SSHUtil;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action that sets a phasphrase and generates a SSH key for KeyBox to use
 */

public class PassphraseAction extends ActionSupport {
    String prevPassphrase;
    String passphrase;
    String passphraseConfirm;
    Boolean hasCustomPassphrase = false;


    @Action(value = "/manage/setPassphrase",
            results = {
                    @Result(name = "success", location = "/manage/set_passphrase.jsp")
            }
    )
    public String setPassphrase() {

        //checks to see if there is a keybox generated passphrase in the DB
        String systemGenPassphrase = PrivateKeyDB.getPassphrase();
        if (systemGenPassphrase == null || systemGenPassphrase.trim().equals("")) {
            hasCustomPassphrase = true;
        }
        return SUCCESS;
    }

    @Action(value = "/passphraseSubmit",
            results = {
                    @Result(name = "input", location = "/manage/set_passphrase.jsp"),
                    @Result(name = "success", location = "/manage/menu.jsp", type = "redirect")
            }
    )
    public String passphraseSubmit() {
        String retVal = SUCCESS;

        if (getPassphrase().equals(getPassphraseConfirm())) {
            SSHUtil.keyGen(passphrase);
            PrivateKeyDB.updatePassphrase(null);
        } else {
            addActionError("Passphrases do not match");
            retVal = INPUT;
        }


        return retVal;
    }

    /**
     * Validates fields for passphrase submit
     */
    public void validatePassphraseSubmit() {
        if (getPassphrase() == null || getPassphrase().trim().equals("")) {
            addFieldError("passphrase", "Required");
        }
        if (getPassphraseConfirm() == null || getPassphraseConfirm().trim().equals("")) {
            addFieldError("passphraseConfirm", "Required");
        }
        //if there is a current custom passphrase then check to see if prev passphrase is valid
        String systemGenPassphrase = PrivateKeyDB.getPassphrase();
        if (systemGenPassphrase == null || systemGenPassphrase.trim().equals("")) {
            //if null then has custom passphrase
            hasCustomPassphrase = true;
            if (getPrevPassphrase() == null || getPrevPassphrase().trim().equals("")) {
                addFieldError("prevPassphrase", "Required");
            }else{
                if (!SSHUtil.isPassphraseValid(getPrevPassphrase())) {
                    addFieldError("prevPassphrase", "Invalid");
                }
            }
        }
    }


    public String getPrevPassphrase() {
        return prevPassphrase;
    }

    public void setPrevPassphrase(String prevPassphrase) {
        this.prevPassphrase = prevPassphrase;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphraseConfirm() {
        return passphraseConfirm;
    }

    public void setPassphraseConfirm(String passphraseConfirm) {
        this.passphraseConfirm = passphraseConfirm;
    }

    public Boolean getHasCustomPassphrase() {
        return hasCustomPassphrase;
    }

    public void setHasCustomPassphrase(Boolean hasCustomPassphrase) {
        this.hasCustomPassphrase = hasCustomPassphrase;
    }


}
