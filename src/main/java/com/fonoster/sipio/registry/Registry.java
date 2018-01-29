package com.fonoster.sipio.registry;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Config;
import com.fonoster.sipio.core.model.Gateway;
import com.fonoster.sipio.repository.GateWayRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gov.nist.javax.sip.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class Registry {
    static final Logger logger = LogManager.getLogger(Registry.class);
    private final Integer checkExpiresTime;
    private final Integer expires;
    private final SipProvider sipProvider;
    private final Config config;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final AddressFactory addressFactory;
    private final ArrayList userAgent;
    private final HashMap<String, RegistryModel> registry;
    int cseq = 0;

    public Registry(SipProvider sipProvider, Integer expires, Integer checkExpiresTime) throws PeerUnavailableException {
        this.expires = expires;
        this.checkExpiresTime = checkExpiresTime;
        this.sipProvider = sipProvider;
        this.config = ConfigManager.getConfig();
        this.messageFactory = SipFactory.getInstance().createMessageFactory();
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
        this.userAgent = new java.util.ArrayList();
        this.userAgent.add(this.config.getUserAgent());
        this.registry = new HashMap();
    }

    public Registry(SipProvider sipProvider) throws PeerUnavailableException {
        this(sipProvider, 300, 1);
    }

    public void requestChallenge(String username, String gwRef, String peerHost, String transport, String received, Integer rport) throws ParseException, InvalidArgumentException {
        String host;
        int port;

        try {
            host = this.sipProvider.getListeningPoint(transport).getIPAddress();
            port = this.sipProvider.getListeningPoint(transport).getPort();
        } catch (Exception e) {
            logger.error("Transport " + transport + " not found in configs => .spec.transport.[*]");
            return;
        }

        if (this.config.getExternalAddress() != null) {
            host = this.config.getExternalAddress();
        }

        if (received != null) host = received;
        if (rport != null) port = rport;

        cseq++;

        List<ViaHeader> viaHeaders = new ArrayList<>();
        ViaHeader viaHeader = this.headerFactory.createViaHeader(host, port, transport, null);
        // Request RPort to enable Symmetric Response in accordance with RFC 3581 and RFC 6314
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);

        MaxForwardsHeader maxForwardsHeader = this.headerFactory.createMaxForwardsHeader(70);
        CallIdHeader callIdHeader = this.sipProvider.getNewCallId();
        CSeqHeader cSeqHeader = this.headerFactory.createCSeqHeader(cseq, Request.REGISTER);
        Address fromAddress = this.addressFactory.createAddress("sip:" + username + "@" + peerHost);
        FromHeader fromHeader = this.headerFactory.createFromHeader(fromAddress, new Utils().generateTag());
        ToHeader toHeader = this.headerFactory.createToHeader(fromAddress, null);
        Address contactAddress = this.addressFactory.createAddress("sip:" + username + "@" + host + ":" + port);
        ContactHeader contactHeader = this.headerFactory.createContactHeader(contactAddress);
        UserAgentHeader userAgentHeader = this.headerFactory.createUserAgentHeader(this.userAgent);
        Header gwRefHeader = this.headerFactory.createHeader("GwRef", gwRef);

        Request request = this.messageFactory.createRequest("REGISTER sip:" + peerHost + " SIP/2.0\r\n\r\n");
        request.addHeader(viaHeader);
        request.addHeader(maxForwardsHeader);
        request.addHeader(callIdHeader);
        request.addHeader(cSeqHeader);
        request.addHeader(fromHeader);
        request.addHeader(toHeader);
        request.addHeader(contactHeader);
        request.addHeader(userAgentHeader);
        request.addHeader(gwRefHeader);
        request.addHeader(this.headerFactory.createAllowHeader("INVITE"));
        request.addHeader(this.headerFactory.createAllowHeader("ACK"));
        request.addHeader(this.headerFactory.createAllowHeader("BYE"));
        request.addHeader(this.headerFactory.createAllowHeader("CANCEL"));
        request.addHeader(this.headerFactory.createAllowHeader("REGISTER"));
        request.addHeader(this.headerFactory.createAllowHeader("OPTIONS"));

        try {
            ClientTransaction clientTransaction = this.sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();
        } catch (Exception e) {
            this.registry.remove(peerHost);

            if (e instanceof javax.sip.TransactionUnavailableException || e instanceof javax.sip.SipException) {
                logger.warn("Unable to register with Gateway -> " + peerHost + ". (Verify your network status)");
            } else {
                logger.warn(e);
            }
        }

        logger.debug(request);
    }

    public void storeRegistry(String username, String host, Integer expires) throws UnknownHostException {
        // Re-register before actual time expiration
        int actualExpires = (int) (expires - 2 * 60 * this.checkExpiresTime);
        RegistryModel reg = new RegistryModel(username, host, InetAddress.getByName(host).getHostAddress(), actualExpires);
        this.registry.put(host, reg);
    }

    public void removeRegistry(String host) {
        this.registry.remove(host);
    }

    public boolean hastHost(String host) {
        return this.registry.get(host) != null;
    }

    public boolean hasIp(String ip) {
        Iterator<RegistryModel> iterator = this.registry.values().iterator();

        while (iterator.hasNext()) {
            RegistryModel reg = iterator.next();
            if (reg.getIp().equals(ip)) return true;
        }
        return false;
    }


    public boolean isExpired(String host) {
        RegistryModel reg = registry.get(host);
        if (reg == null) return true;

        long elapsed = Duration.between(LocalDateTime.now(), reg.getRegisteredOn()).getSeconds();
        return (reg.getExpires() - elapsed) <= 0;
    }

    public void start() {
        logger.info("Starting Registry service");
        HashMap<String, RegistryModel> registry = this.registry;

        final Registry myRegistry = this;
        TimerTask registerTask = new TimerTask() {
            @Override
            public void run() {
                List<Gateway> result = GateWayRepository.getGateways();
                if (result == null) return;
                for (Gateway gateway : result) {
                    if (isExpired(gateway.getHost())) {
                        logger.info("Register with " + gateway.getName() + " using "
                                + gateway.getUserName() + "@" + gateway.getHost());

                        try {
                            myRegistry.requestChallenge(gateway.getUserName(),
                                    gateway.getRef(), gateway.getHost(), gateway.getTransport(), null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if (!gateway.getRegistries().isEmpty()) {
                            for (String registry : gateway.getRegistries()) {

                                if (isExpired(gateway.getHost())) {
                                    logger.debug("Register with " + gateway.getName() + " using " + gateway.getUserName() + "@" + registry);
                                    try {
                                        myRegistry.requestChallenge(gateway.getUserName(), gateway.getRef(), registry, gateway.getTransport(), null, null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        new Timer().schedule(registerTask, 10000, (this.checkExpiresTime * 60 * 1000));
    }

    public void stop() {

    }

}
