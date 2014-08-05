package net.sf.xapp.net.server.framework.email;

public interface MailProxy
{
    void sendMail(String message, String subject, String to);
}
