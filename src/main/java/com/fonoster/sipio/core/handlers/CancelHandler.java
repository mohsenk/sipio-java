package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.Context;
import com.fonoster.sipio.core.ContextStorage;

import javax.sip.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.List;

public class CancelHandler {

    SipProvider sipProvider;
    ContextStorage contextStorage;
    MessageFactory messageFactory;

    public CancelHandler(SipProvider sipProvider, ContextStorage contextStorage) throws PeerUnavailableException {
        this.sipProvider = sipProvider;
        this.contextStorage = contextStorage;
        this.messageFactory = SipFactory.getInstance().createMessageFactory();
    }


    public void cancel(Request request, ServerTransaction serverTransaction) throws ParseException, SipException, InvalidArgumentException {
        List<Context> storage = this.contextStorage.getStorage();
        for (Context context : storage) {
            if (context.getServerTransaction() != null && context.getServerTransaction().getBranchId().equals(serverTransaction.getBranchId())) {
                Request originRequest = context.getRequestIn();
                Response originResponse = this.messageFactory.createResponse(Response.REQUEST_TERMINATED, originRequest);
                Response cancelResponse = this.messageFactory.createResponse(Response.OK, request);
                Request cancelRequest = context.getClientTransaction().createCancel();
                ClientTransaction clientTransaction = this.sipProvider.getNewClientTransaction(cancelRequest);
                context.getServerTransaction().sendResponse(originResponse);
                serverTransaction.sendResponse(cancelResponse);
                clientTransaction.sendRequest();
            }
        }
    }
}
