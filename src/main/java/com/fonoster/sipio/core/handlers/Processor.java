package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.ContextStorage;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.registry.Registry;
import gov.nist.javax.sip.RequestEventExt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.sip.*;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class Processor implements SipListener {

    static final Logger logger = LogManager.getLogger(Processor.class);

    private final SipProvider sipProvider;
    private final Locator locator;
    private final Registrar registrar;
    private final Registry registry;
    private final RequestProcessor requestProcessor;
    private final ResponseProcessor responseProcessor;
    private ContextStorage contextStorage;

    public Processor(SipProvider sipProvider, Locator locator, Registry registry, Registrar registrar, ContextStorage contextStorage) throws PeerUnavailableException, NoSuchAlgorithmException {
        this.sipProvider = sipProvider;
        this.locator = locator;
        this.contextStorage = contextStorage;
        this.registrar = registrar;
        this.registry = registry;
        this.requestProcessor = new RequestProcessor(sipProvider, locator, registry, registrar, contextStorage);
        this.responseProcessor = new ResponseProcessor(sipProvider, locator, registry, registrar, contextStorage);
    }


    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            logger.info("Process Request {}",requestEvent.getRequest().getMethod());
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
        } catch (SipException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
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
                logger.trace("Ongoing Transaction");
            }
        }
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent event) {
        logger.trace("Dialog {}" + event.getDialog() + " has been terminated");
    }
}
