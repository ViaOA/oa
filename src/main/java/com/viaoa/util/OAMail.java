/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.util;


import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

public class OAMail implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    private String host;
    private int port;
    private String userId;
    private String password;
    private boolean bDebug;

    
    public static class OAMailAttachment {
        public byte[] bsData;
        public String mimeType;
        public String fileName;
    }
    

    /**
     * Used to send emails.
     * @param host 
     * @param port if < 1, then it will use default 25
     * @param user  user id for mail server
     * @param pw  user password for mail server
     */
    public OAMail(String host, int port, String user, String pw) {
        this.host = host;
        this.port = port;
        this.userId = user;
        this.password = pw;
    }

    
    public void setDebug(boolean b) {
        this.bDebug = b;

    }

    public void sendSmtp( 
            String to, String cc, String from, 
            String subject, String text,
            String contentType,
            String[] fileNames) throws Exception 
    {
        sendSmtp(new String[] {to}, new String[] {cc}, from, subject, text, contentType, fileNames);
    }    

    public void sendSmtp( 
            String to, String cc, String from, 
            String subject, String text,
            String contentType,
            byte[] bsAttachment, String mimeTypeBs, String bsFileName) throws Exception 
    {
        sendSmtp(new String[] {to}, new String[] {cc}, from, subject, text, contentType, null, bsAttachment, mimeTypeBs, bsFileName);
    }    
    
    /**
     * 
     * @param to
     * @param cc
     * @param from
     * @param subject
     * @param text
     * @param contentType if null or blank, then will default to "text/html; charset=UTF-8"
     *              , others: "text/html", "text/plain", "text/richtext", "text/css", "image/gif"
     * @param fileNames
     * @throws Exception
     */
    public void sendSmtp( 
            String[] to, String[] cc, String from, 
            String subject, String text,
            String contentType,
            String[] fileNames) throws Exception 
    {
        sendSmtp(to, cc, from, subject, text, contentType, fileNames, null, null, null);
    }
    public void sendSmtp( 
            String[] to, String[] cc, String from, 
            String subject, String text,
            String contentType,
            byte[] bsAttachment, String mimeTypeBs, String bsFileName) throws Exception 
    {
        sendSmtp(to, cc, from, subject, text, contentType, null, bsAttachment, mimeTypeBs, bsFileName);
    }
    
    public void sendSmtp( 
            String[] to, String[] cc, String from, 
            String subject, String text,
            String contentType,
            String[] fileNames, byte[] bsAttachment, String mimeTypeBs, String bsFileName) throws Exception 
    {
        OAMailAttachment[] mas = null;
        if (bsAttachment != null) {
            mas = new OAMailAttachment[1];
            OAMailAttachment ma = new OAMailAttachment();
            ma.bsData = bsAttachment;
            ma.fileName= bsFileName;
            ma.mimeType = mimeTypeBs;
            mas[0] = ma;
        }
        sendSmtp(to, cc, from, subject, text, contentType, fileNames, mas);
    }
    
    
    public void sendSmtp( 
            String[] to, String[] cc, String from, 
            String subject, String text,
            String contentType,
            String[] fileNames, OAMailAttachment[] attachments) throws Exception 
    {
        String msg = "";
        if (from == null) from = "";
        Transport transport = null;
        if (port < 1) port = 25;

        if (contentType == null || contentType.length() == 0) {
            // contentType = "text/html; charset=iso-8859-1";
            contentType = "text/html; charset=UTF-8";
        }
        
        
        // create some properties and get the default Session
        Properties props = System.getProperties();
        
        props.put("mail.smtp.auth", "true"); // required
        //props.put("mail.smtp.host", host);
        // if (port > 0) props.put("mail.smtp.port", port+""); // default 25
        // props.put("mail.smtp.user", "viaoa.com@noip-smtp");
        // props.put("mail.smtp.password", "vincePw"); 
        // props.setProperty ("mail.transport.protocol", "smtp");             
        // props.setProperty("mail.smtp.starttls.enable", "true"); 
        // see: http://javamail.kenai.com/nonav/javadocs/com/sun/mail/smtp/package-summary.html

        /*
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("viaoa.com@noip-smtp", "vincePw");
            }
        };
        */       
        
        Session session = Session.getInstance(System.getProperties());
        // Session session = Session.getDefaultInstance(System.getProperties(), null);
        // Session session = Session.getInstance(System.getProperties(), auth);
        session.setDebug(bDebug);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        for (int i=0; to != null && i<to.length; i++) {
            if (!OAString.isEmpty(to[i])) {
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to[i]));
            }
        }
        for (int i=0; cc != null && i<cc.length; i++) {
            if (!OAString.isEmpty(cc[i])) {
                message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc[i]));
            }
        }
        message.setSubject(subject==null?"":subject);
        //message.setContent(text+host,"text/html; charset=iso-8859-1"); // HTML ??
        
        // create and fill the first message part

        MimeBodyPart mbp1 = new MimeBodyPart();
        //mbp1.setText(text);
        
        mbp1.setContent( text, contentType);  // 20150925
        // was: mbp1.setContent( text, "text/html; charset=iso-8859-1" );
        
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);


        if (fileNames != null) {
            for (int i=0; i<fileNames.length; i++) {
                String filename = fileNames[i];
                // attach the file to the message
                FileDataSource fds = new FileDataSource(filename);
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                mp.addBodyPart(mbp2);    // add the Multipart to the message
            }
        }

        
        if (attachments != null) {
            for (OAMailAttachment attachment : attachments) {
                ByteArrayDataSource bads = new ByteArrayDataSource(attachment.bsData, attachment.mimeType);
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.setDataHandler(new DataHandler(bads));
                mbp2.setFileName(attachment.fileName);
                mp.addBodyPart(mbp2);    // add the Multipart to the message
            }
        }
        
        message.setContent(mp);
        message.setSentDate(new java.util.Date());
        message.saveChanges();
        
        transport = session.getTransport("smtp");
        transport.connect(host, port, userId, password);
        transport.sendMessage(message, message.getAllRecipients());

        transport.close();
        //was: Transport.send(message);
    }

    
    public boolean isValidEmailAddress(String email) {
        if (email == null || email.length() == 0) return false;
        
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
    

    public static void main(String[] args) throws Exception {
        String msg = "";
        String contentType = "text/html; charset=UTF-8";
        
        OAMail m = new OAMail("smtp-auth.no-ip.com", 3325, "viaoa.com@noip-smtp", "vincePW");
        m.setDebug(true);
        // m.sendSmtp(new String[]{"vincent.via@theice.com"}, null, "vince@viaoa.com", "subject", "text", null, new String[] {"c:\\temp\\cem.jpg"});
        
        
        m = new OAMail("mail.gohi5.com", 2525, "notifications@gohi5.com","pnruf013" );
        // "mail.gohi5.com", "notifications","pnruf013"
        m.setDebug(true);

        
        // hi5: "mail.gohi5.com", "notifications","pnruf013"
        // Jmadden <jmadden@gohi5.com>
        m.sendSmtp(new String[]{"vincent.via@theice.com"}, 
                new String[]{"jmadden@gohi5.com", "lukkireddy@laratekies.com"}, "jmadden@gohi5.com", 
                "Email from Hi5 Server", 
                "<html><body>This is <i>another</i> email from the <h3>goHi5</h3> server, with an attachment</body></html>", null, 
                new String[] {"c:\\temp\\cem.jpg"});
    }

}





