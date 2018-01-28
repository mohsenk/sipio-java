package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.AccountManagerService;
import com.fonoster.sipio.core.Context;
import com.fonoster.sipio.core.ContextStorage;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.registry.Registry;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.net.UnknownHostException;

public class ResponseProcessor {

    static final Logger logger = LogManager.getLogger(ResponseProcessor.class);
    private final HeaderFactory headerFactory;

    private AccountManagerService accountManagerService;
    private SipProvider sipProvider;
    private Registry registry;
    private ContextStorage contextStorage;

    public ResponseProcessor(SipProvider sipProvider, Locator locator, Registry registry, Registrar registrar, ContextStorage contextStorage) throws PeerUnavailableException {
        this.sipProvider = sipProvider;
        this.registry = registry;
        this.contextStorage = contextStorage;
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
        this.accountManagerService = new AccountManagerService();
    }

    public void process(ResponseEvent event) throws SipException, InvalidArgumentException, UnknownHostException {
        Response responseIn = event.getResponse();
        CSeqHeader cseq = (CSeqHeader) responseIn.getHeader(CSeqHeader.NAME);
        ExpiresHeader expiresHeader = (ExpiresHeader) responseIn.getHeader(ExpiresHeader.NAME);
        FromHeader fromHeader = (FromHeader) responseIn.getHeader(FromHeader.NAME);
        SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();
        ViaHeader viaHeader = (ViaHeader) responseIn.getHeader(ViaHeader.NAME);
        ClientTransaction clientTransaction = event.getClientTransaction();



        // The stack takes care of this cases
        if (responseIn.getStatusCode() == Response.TRYING ||
                responseIn.getStatusCode() == Response.REQUEST_TERMINATED ||
                cseq.getMethod().equals(Request.CANCEL)) return;

        if (cseq.getMethod().equals(Request.REGISTER) &&
                responseIn.getStatusCode() == Response.OK) {

            Request request = clientTransaction.getRequest();
            String gwRef = request.getHeader("GwRef").toString();

            int rPort = viaHeader.getRPort();
            int port = viaHeader.getPort();
            String host = viaHeader.getHost();
            String received = viaHeader.getReceived();

            if ((received != null && !host.equals(received)) || port != rPort) {
                String username = fromURI.getUser();
                String transport = viaHeader.getTransport().toLowerCase();
                // This may not be the best source to get this parameter
                String peerHost = fromURI.getHost();

                logger.debug("Sip I/O is behind a NAT. Re-registering using Received and RPort");
                try {
                    this.registry.requestChallenge(username, gwRef, peerHost, transport, received, rPort);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            int expires = 300;
            if(expiresHeader != null) {
                expires = expiresHeader.getExpires();
            }
            this.registry.storeRegistry(fromURI.getUser(), fromURI.getHost(), expires);
        } else if(cseq.getMethod().equals(Request.REGISTER)) {
            this.registry.removeRegistry(fromURI.getHost());
        }

        // WARNING: This is causing an issue with tcp transport and DIDLogic
        // I believe that DIDLogic does not fully support tcp registration
        if (responseIn.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED ||
                responseIn.getStatusCode() == Response.UNAUTHORIZED) {
            SipStackImpl sipStack = (SipStackImpl) this.sipProvider.getSipStack();

            AuthenticationHelper authenticationHelper = sipStack.getAuthenticationHelper(this.accountManagerService, this.headerFactory);
            ClientTransaction t = authenticationHelper.handleChallenge(responseIn, clientTransaction, (SipProvider) event.getSource(), 5);
            t.sendRequest();
            logger.debug(responseIn);
            return;
        }

        // Strip the topmost via header
        Response responseOut = (Response) responseIn.clone();
        responseOut.removeFirst(ViaHeader.NAME);

        if (cseq.getMethod().equals(Request.INVITE) && clientTransaction != null) {
            // In theory we should be able to obtain the ServerTransaction casting the ApplicationData.
            // However, I'm unable to find the way to cast this object.
            //let st = clientTransaction.getApplicationData()'
            Context context = this.contextStorage.findContext(clientTransaction);

            if (context != null && context.getServerTransaction() != null) context.getServerTransaction().sendResponse(responseOut);

        } else {
            // Could be a BYE due to Record-Route
            // There is no more Via headers; the response was intended for the proxy.
            if (responseOut.getHeader(ViaHeader.NAME) != null) this.sipProvider.sendResponse(responseOut);
        }
        logger.debug(responseOut);
    }
}
