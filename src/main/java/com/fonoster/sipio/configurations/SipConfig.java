package com.fonoster.sipio.configurations;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.Transport;
import com.fonoster.sipio.core.model.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sip.*;
import java.util.Properties;

@Configuration
public class SipConfig {
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_RESET = "\u001B[0m";
    static final Logger logger = LoggerFactory.getLogger(SipConfig.class);

    @Bean
    public SipProvider build() throws Exception {
        SipFactory sipFactory = SipFactory.getInstance();
        Properties properties = new Properties();

        Config config = ConfigManager.getConfig();

        // See https://github.com/RestComm/jain-sip/blob/master/src/gov/nist/javax/sip/SipStackImpl.java for
        // many other options
        sipFactory.setPathName("gov.nist");
        properties.setProperty("javax.sip.STACK_NAME", "sipio");
        // Default host
        properties.setProperty("javax.sip.IP_ADDRESS", config.getBindAddr());
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "OFF");
        // Guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        // Drop the client connection after we are done with the transaction.;
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", config.getTraceLevel().toString());
        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", "gov.nist.javax.sip.stack.NioMessageProcessorFactory");
        properties.setProperty("gov.nist.javax.sip.PATCH_SIP_WEBSOCKETS_HEADERS", "false");

        // See https://groups.google.com/forum/#!topic/mobicents-public/U_c7aLAJ_MU for useful info
        if (true) {
            properties.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", String.join(",", config.getProtocols()));
            // This must be set to "Disabled" when using WSS
            properties.setProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE", config.getAuthType());
            properties.setProperty("javax.net.ssl.keyStore", config.getKeyStore());
            properties.setProperty("javax.net.ssl.trustStore", config.getTrustStore());
            properties.setProperty("javax.net.ssl.keyStorePassword", config.getKeyStorePassword());
            properties.setProperty("javax.net.ssl.keyStoreType", config.getKeyStoreType());

            if (config.getSSLDebugging()) {
                System.setProperty("javax.net.debug", "ssl");
            }
        }


        Transport firstTransport = config.getTransport().get(0);

        SipStack sipStack = sipFactory.createSipStack(properties);
        ListeningPoint defTransport = sipStack.createListeningPoint(5060, firstTransport.getProtocol().toLowerCase());
        SipProvider sipProvider = sipStack.createSipProvider(defTransport);


        if (config.getExternalAddress() != null)
            logger.info("ExternAddr @ " + ANSI_GREEN + config.getExternalAddress() + ANSI_RESET);
        for (Transport transport : config.getTransport()) {
            String proto = transport.getProtocol().toLowerCase();

            if (transport.getAddress() == null) transport.setAddress(config.getBindAddr());

            ListeningPoint lp = sipStack.createListeningPoint(transport.getAddress(), transport.getPort(), proto);
            sipProvider.addListeningPoint(lp);

            logger.info("Listening  @ " + ANSI_GREEN + transport.getAddress()
                    + ":" + transport.getPort()
                    + " [" + proto + "]"
                    + ANSI_RESET);
        }
        return sipProvider;
    }
}
