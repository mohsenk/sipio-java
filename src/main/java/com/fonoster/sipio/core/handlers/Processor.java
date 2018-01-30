package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.ContextStorage;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.registry.GatewayConnector;
import gov.nist.javax.sip.RequestEventExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import java.security.NoSuchAlgorithmException;

public class Processor implements SipListener {



    static final Logger logger = LoggerFactory.getLogger(Processor.class);
    private final RequestProcessor requestProcessor;
    private final ResponseProcessor responseProcessor;
    private ContextStorage contextStorage;

    public Processor(SipProvider sipProvider, Locator locator, GatewayConnector gatewayConnector, Registrar registrar, ContextStorage contextStorage) throws PeerUnavailableException, NoSuchAlgorithmException {
        this.contextStorage = contextStorage;
        this.requestProcessor = new RequestProcessor(sipProvider, locator, gatewayConnector, registrar, contextStorage);
        this.responseProcessor = new ResponseProcessor(sipProvider, locator, gatewayConnector, registrar, contextStorage);
    }


    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            requestProcessor.process((RequestEventExt) requestEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        logger.info("Process Response {}",responseEvent.getResponse().getContent());
        try {
            responseProcessor.process(responseEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        logger.trace("Transaction Time out");
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("Transaction IOException");
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent event) {
        if (event.isServerTransaction()) {
            ServerTransaction serverTransaction = event.getServerTransaction();

            if (!this.contextStorage.removeContext(serverTransaction)) {
                logger.info("Ongoing Transaction");
            }
        }
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent event) {
        logger.trace("Dialog {}" + event.getDialog() + " has been terminated");
    }
}
