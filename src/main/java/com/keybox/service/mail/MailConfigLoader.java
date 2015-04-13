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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class MailConfigLoader {
	
	private static PropertiesConfiguration mailprop;
	private static PropertiesConfiguration default_mailprop;
	
	/**
	 * Constructor for MailConfigLoader
	 *  
	 * @param mailfile Filename from the Mail-Properties in resources
	 */
	public MailConfigLoader(String mailfile) {
		try {
			mailprop = new PropertiesConfiguration(MailConfigLoader.class.getClassLoader().getResource(".").getPath() + "/"+ mailfile);
			default_mailprop = new PropertiesConfiguration(MailConfigLoader.class.getClassLoader().getResource(".").getPath() + "/mail.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Getter for Mail Config Propertys
	 * 
	 * @param name
	 * @return
	 */
	public String getProperty(String name) {
		String propertie = mailprop.getString(name);
		if(propertie == null)
		{
			propertie = default_mailprop.getString(name);
		}
        return propertie;
    }

}
