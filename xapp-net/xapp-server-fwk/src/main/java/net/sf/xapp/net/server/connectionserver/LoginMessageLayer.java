/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.api.userapi.UserApi;
import net.sf.xapp.net.api.userapi.to.*;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.UserId;
import org.apache.log4j.Logger;

public class LoginMessageLayer<T> implements MessageLayer<T, UserId> {
    private final UserApi userApi;
    private final MessageLayer<T, UserId> delegate;
    private IOLayer<T, UserId> ioLayer;
    private Logger log = Logger.getLogger(getClass());

    public LoginMessageLayer(MessageLayer<T, UserId> delegate, UserApi userApi) {
        this.userApi = userApi;
        this.delegate = delegate;
    }

    @Override
    public void setIOLayer(IOLayer<T, UserId> ioLayer) {
        this.ioLayer = ioLayer;
        delegate.setIOLayer(ioLayer);
    }

    @Override
    public void sessionOpened(T session) {
    }

    @Override
    public void sessionClosed(T session) {
        if (ioLayer.getSessionKey(session) != null) {
            delegate.sessionClosed(session);
        }
    }

    @Override
    public void handleMessage(T session, InMessage message) {
        UserId storedUserId = ioLayer.getSessionKey(session);
        UserId principal = (UserId) message.principal();
        if (storedUserId == null) {
            if (message instanceof LoginWithToken) {
                LoginWithToken login = (LoginWithToken) message;
                ioLayer.setSessionKey(session, login.getUserId());
                login.visit(userApi);
                //TODO trap error and force disconnect
                log.info(login.getUserId() + " successfully logged in");
                delegate.sessionOpened(session);
            } else if (message instanceof ResetPassword) {
                ResetPassword resetPassword = (ResetPassword) message;
                resetPassword.visit(userApi);
            } else if (message instanceof Login) {
                Login login = (Login) message;
                try {
                    LoginResponse loginResponse = login.visit(userApi);
                    log.info(login.getNickname() + " successfully logged in");
                    ioLayer.sendMessage(session, loginResponse);
                } catch (GenericException e) {
                    log.info(login.getNickname() + ", failed login attempt", e);
                    ioLayer.sendMessage(session, new LoginResponse(null, e.getErrorCode()));
                }
            } else if (message instanceof LoginAsGuest) {
                LoginAsGuest loginAsGuest = (LoginAsGuest) message;
                try {
                    LoginAsGuestResponse response = loginAsGuest.visit(userApi);
                    log.info(loginAsGuest.getNickname() + " successfully logged in as guest");
                    ioLayer.sendMessage(session, response);
                } catch (GenericException e) {
                    log.info(loginAsGuest.getNickname() + ", failed login attempt as guest", e);
                    ioLayer.sendMessage(session, new LoginAsGuestResponse(null, e.getErrorCode()));
                }
            } else if (message instanceof SignUp) {
                SignUp signUp = (SignUp) message;
                try {
                    SignUpResponse response = signUp.visit(userApi);
                    log.info("new sign up: " + signUp);
                    ioLayer.sendMessage(session, response);
                } catch (GenericException e) {
                    log.info("failed sign up: " + signUp, e);
                    ioLayer.sendMessage(session, new SignUpResponse(null, e.getErrorCode()));
                }
            } else {
                log.warn("user has not logged in, " + session);
                ioLayer.closeSession(session);
            }
        } else {
            if (storedUserId.equals(principal)) //generify "principal field"
            {
                delegate.handleMessage(session, message);
            } else {
                log.warn("principal field did not match stored principal for this connection");
                ioLayer.closeSession(session);
            }
        }
    }
}
