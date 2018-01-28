package com.fonoster.sipio.registry;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gov.nist.javax.sip.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Registry {
    static final Logger logger = LogManager.getLogger(Registry.class);
    private final Float checkExpiresTime;
    private final Integer expires;
    private final SipProvider sipProvider;
    private final Config config;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final AddressFactory addressFactory;
    private final ArrayList userAgent;
    private final HashMap<String, RegistryModel> registry;
    int cseq = 0;

    public Registry(SipProvider sipProvider, Integer expires, Float checkExpiresTime) throws PeerUnavailableException {
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
        this(sipProvider,300,0.5f);
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

    public String listAsJSON() {
        List<RegistryModel> s = new ArrayList<>();
        Iterator<RegistryModel> iterator = this.registry.values().iterator();
        while (iterator.hasNext()) {
            RegistryModel reg = iterator.next();
            s.add(reg);
        }
        return new Gson().toJson(s).toString();
    }

    public void start() {
        logger.info("Starting Registry service");
//        var registry = this.registry;
//        var gatewaysAPI = this.gatewaysAPI
//        // var myRegistry = new Registry(this.sipProvider, this.dataAPIs)
//        var myRegistry = this
//
//        function isExpired (host) {
//            const reg = registry.get(host)
//
//        if (reg == null) return true
//
//            const elapsed = (Date.now() - reg.registeredOn) / 1000
//        if ((reg.expires - elapsed) <= 0) {
//            return true
//        }
//        return false
//        }
//
//        let registerTask = new java.util.TimerTask({
//                run:function() {
//                const result = gatewaysAPI.getGateways()
//            if (result.status != Status.OK) return
//
//                    result.obj.forEach(function(gateway) {
//                let regService = gateway.spec.regService
//
//                if (isExpired(regService.host)) {
//                    LOG.debug("Register with " + gateway.metadata.name + " using "
//                            + gateway.spec.regService.credentials.username + "@" + gateway.spec.regService.host)
//                    myRegistry.requestChallenge(regService.credentials.username,
//                            gateway.metadata.ref, regService.host, regService.transport)
//                }
//
//                let registries = gateway.spec.regService.registries
//
//                if (registries != undefined) {
//                    registries.forEach(function(h) {
//                        if (isExpired(regService.host)) {
//                            LOG.debug("Register with " + gateway.metadata.name + " using " + gateway.spec.regService.credentials.username + "@" + h)
//                            myRegistry.requestChallenge(gateway.spec.regService.credentials.username, gateway.metadata.ref, h, gateway.spec.regService.transport)
//                        }
//                    })
//                }
//            })
//        }
//        })
//
//        new java.util.Timer().schedule(registerTask, 10000, this.checkExpiresTime * 60 * 1000)
    }

    public void stop() {

    }

}
