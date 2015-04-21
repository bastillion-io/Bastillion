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
package com.keybox.service.mail;

public class MassageParamter {

	private String pattern;
	private String value;
	/**
	 * Constructor for MassageParamter
	 * 
	 * @param pattern pattern of variable
	 * @param value value variable
	 */
	public MassageParamter(String pattern, String value) {
		this.pattern = pattern;
		this.value = value;
	}
	/**
	 * Getter for Pattern
	 * 
	 * @return Pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Setter for Pattern
	 * 
	 * @param pattern pattern of variable
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	/**
	 * Getter for Value
	 * @return
	 */
	public String getValue() {
		return value;
	}
	/**
	 * Setter for Value
	 * @param value value variable
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
