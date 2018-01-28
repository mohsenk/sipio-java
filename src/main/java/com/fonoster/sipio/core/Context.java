package com.fonoster.sipio.core;

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;

public class Context {

    ClientTransaction clientTransaction;
     ServerTransaction serverTransaction;
    String method;
    Request requestIn;
    Request requestOut;

    public ClientTransaction getClientTransaction() {
        return clientTransaction;
    }

    public void setClientTransaction(ClientTransaction clientTransaction) {
        this.clientTransaction = clientTransaction;
    }

    public ServerTransaction getServerTransaction() {
        return serverTransaction;
    }

    public void setServerTransaction(ServerTransaction serverTransaction) {
        this.serverTransaction = serverTransaction;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Request getRequestIn() {
        return requestIn;
    }

    public void setRequestIn(Request requestIn) {
        this.requestIn = requestIn;
    }

    public Request getRequestOut() {
        return requestOut;
    }

    public void setRequestOut(Request requestOut) {
        this.requestOut = requestOut;
    }
}
