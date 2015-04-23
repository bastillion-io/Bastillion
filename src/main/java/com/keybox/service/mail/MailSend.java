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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSend {

	/**
	 * Method to send Mail
	 * 
	 * @param mailart Filename from the Mail-Properties in resources
	 * @param recipientMail Recipient Mail Address
	 * @param recipientName Recipient Name
	 * @param massageParameter Parameter List for message and subject
	 * @throws MessagingException Mail send fail
	 * @throws UnsupportedEncodingException EMail address not supported
	 */
	public static void sendMail(String mailart, String recipientMail, String recipientName, ArrayList<MassageParamter> massageParameter) throws UnsupportedEncodingException, MessagingException {
		MailConfigLoader mcl = new MailConfigLoader(mailart);
		
		//Set Configuration when sending mail
		final Properties props = new Properties();
		props.put("mail.smtp.host", mcl.getProperty("mail.smtp.host"));
		props.put("mail.smtp.port", mcl.getProperty("mail.smtp.port"));
		props.put("mail.transport.protocol",mcl.getProperty("mail.transport.protocol"));
		props.put("mail.smtp.auth", mcl.getProperty("mail.smtp.auth"));
		props.put("mail.smtp.starttls.enable", mcl.getProperty("mail.smtp.starttls.enable"));
		props.put("mail.smtp.tls", mcl.getProperty("mail.smtp.tls"));
		props.put("mail.smtp.ssl.checkserveridentity", mcl.getProperty("mail.smtp.ssl.checkserveridentity"));
		MailAuthenticator mailauth = new MailAuthenticator(mcl.getProperty("mail.auth.user"), mcl.getProperty("mail.auth.pw"));
		Session session = Session.getDefaultInstance(props, mailauth);
		 
		
		
		//Change message and subject text 
		String subjectText = mcl.getProperty("mail.subject");
		for (MassageParamter mp : massageParameter) {
			subjectText = subjectText.replace(mp.getPattern(), mp.getValue());
		}
		String messageText = mcl.getProperty("mail.message");
		for (MassageParamter mp : massageParameter) {
			messageText = messageText.replace(mp.getPattern(), mp.getValue());
		}
		
		
		// Build and send a message
		Message msg = new MimeMessage(session);
		
		msg.setFrom(new InternetAddress(mcl.getProperty("mail.address"), mcl.getProperty("mail.person")));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientMail, recipientName));
		msg.setSubject(subjectText);
		msg.setText(messageText);
		msg.saveChanges();
		Transport.send(msg);
	
		
	}
}
