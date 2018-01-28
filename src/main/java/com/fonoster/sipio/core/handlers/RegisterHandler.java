package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.utils.AuthHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sip.*;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class RegisterHandler {


    static final Logger logger = LogManager.getLogger(RegisterHandler.class);

    private Locator locator;
    private Registrar registrar;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AuthHelper authHelper;

    public RegisterHandler(Locator locator, Registrar registrar) throws PeerUnavailableException {
        this.locator = locator;
        this.registrar = registrar;
        this.messageFactory = SipFactory.getInstance().createMessageFactory();
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
        this.authHelper = new AuthHelper(this.headerFactory);

    }

    public void register(Request request, ServerTransaction transaction) throws Exception {
        ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
        URI contactURI = contactHeader.getAddress().getURI();
        AuthorizationHeader authHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
        URI addressOfRecord = toHeader.getAddress().getURI();

        int expires;

        if (request.getExpires() != null) {
            expires = request.getExpires().getExpires();
        } else {
            expires = contactHeader.getExpires();
        }

        ExpiresHeader expH = this.headerFactory.createExpiresHeader(expires);

        if (contactHeader.getAddress().isWildcard() && expires <= 0) {
            this.locator.removeEndpoint(addressOfRecord);
            Response ok = this.messageFactory.createResponse(Response.OK, request);
            ok.addHeader(contactHeader);
            ok.addHeader(expH);
            transaction.sendResponse(ok);
            return;
        } else if (!contactHeader.getAddress().isWildcard() && expires <= 0) {
            this.locator.removeEndpoint(addressOfRecord, contactURI);
            Response ok = this.messageFactory.createResponse(Response.OK, request);
            ok.addHeader(contactHeader);
            ok.addHeader(expH);
            transaction.sendResponse(ok);
            return;
        }

        if (authHeader == null) {
            Response unauthorized = this.messageFactory.createResponse(Response.UNAUTHORIZED, request);
            unauthorized.addHeader(this.authHelper.generateChallenge());
            transaction.sendResponse(unauthorized);
            logger.debug(unauthorized);
        } else {
            if (this.registrar.register(request)) {
                Response ok = this.messageFactory.createResponse(Response.OK, request);
                ok.addHeader(contactHeader);
                ok.addHeader(expH);
                transaction.sendResponse(ok);
                logger.debug(ok);
            } else {
                Response unauthorized = this.messageFactory.createResponse(Response.UNAUTHORIZED, request);
                unauthorized.addHeader(this.authHelper.generateChallenge());
                transaction.sendResponse(unauthorized);
                logger.debug(unauthorized);
            }
        }

    }
}
