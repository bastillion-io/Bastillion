package com.keybox.manage.model;

/**
 * Value object that contains script information
 */
public class Script {
    Long id;
    String script="#!/bin/bash\n\n";
    String displayNm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getDisplayNm() {
        return displayNm;
    }

    public void setDisplayNm(String displayNm) {
        this.displayNm = displayNm;
    }
}
