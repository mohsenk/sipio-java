package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.utils.AuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.PeerUnavailableException;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public class RegisterHandler {


    static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    private Locator locator;
    private Registrar registrar;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AuthHelper authHelper;

    public RegisterHandler(Locator locator, Registrar registrar) throws PeerUnavailableException, NoSuchAlgorithmException {
        this.locator = locator;
        this.registrar = registrar;
        this.messageFactory = SipFactory.getInstance().createMessageFactory();
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
        this.authHelper = new AuthHelper();

    }

    public void register(Request request, ServerTransaction transaction) throws Exception {
        ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);

        AuthorizationHeader authHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
        SipURI addressOfRecord = (SipURI) toHeader.getAddress().getURI();
        int expires;

        if (request.getExpires() != null) {
            expires = request.getExpires().getExpires();
        } else {
            expires = contactHeader.getExpires();
        }

        ExpiresHeader expH = this.headerFactory.createExpiresHeader(expires);

        if (expires <= 0 && expired(request, transaction, expires)) {
            return;
        }

        String  realm = addressOfRecord.getHost();

        if (authHeader == null) {
            Response response = buildAccessDeniedResponse(request,realm);
            transaction.sendResponse(response);
            logger.info("Response to register request : {}", response);
            return;
        }

        if (this.registrar.register(request)) {
            Response ok = this.messageFactory.createResponse(Response.OK, request);
            ok.addHeader(contactHeader);
            ok.addHeader(expH);
            transaction.sendResponse(ok);
            logger.info("Response to register request : {}", ok);
        } else {
            Response response = buildAccessDeniedResponse(request,realm);
            transaction.sendResponse(response);
            logger.info("Response to register request : {}", response);
        }


    }

    public Response buildAccessDeniedResponse(Request request,String realm) throws ParseException, NoSuchAlgorithmException {
        Response unauthorized = this.messageFactory.createResponse(Response.UNAUTHORIZED, request);
        unauthorized.addHeader(this.authHelper.generateChallenge(headerFactory,realm));
        return unauthorized;
    }

    public boolean expired(Request request, ServerTransaction transaction, int expires) throws Exception {
        ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
        URI contactURI = contactHeader.getAddress().getURI();
        URI addressOfRecord = toHeader.getAddress().getURI();
        ExpiresHeader expH = this.headerFactory.createExpiresHeader(expires);
        if (contactHeader.getAddress().isWildcard()) {
            this.locator.removeEndpoint(addressOfRecord);
            Response ok = this.messageFactory.createResponse(Response.OK, request);
            ok.addHeader(contactHeader);
            ok.addHeader(expH);
            transaction.sendResponse(ok);
            logger.info("", ok);
            return true;
        } else if (!contactHeader.getAddress().isWildcard() && expires <= 0) {
            this.locator.removeEndpoint(addressOfRecord, contactURI);
            Response ok = this.messageFactory.createResponse(Response.OK, request);
            ok.addHeader(contactHeader);
            ok.addHeader(expH);
            transaction.sendResponse(ok);
            logger.info("{}", ok);
            return true;
        }

        return false;
    }
}
