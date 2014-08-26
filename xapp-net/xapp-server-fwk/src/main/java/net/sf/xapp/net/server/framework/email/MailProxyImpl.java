package net.sf.xapp.net.server.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailProxyImpl extends Authenticator implements MailProxy
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Properties props;

    public MailProxyImpl()
    {
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
    }

    @Override
    public void sendMail(String content, String subject, String to)
    {
        if (Boolean.getBoolean("live"))
        {
            sendMailInternal(content, subject, to);
        }
        else
        {
            log.info("skipping sending of " + content + " because not live config");
        }
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication("support@pokatron.com", "VB1BV2abc");
    }

    private void sendMailInternal(String content, String subject, String to)
    {
        Session session = Session.getDefaultInstance(props, this);

        try
        {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("support@pokatron.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
