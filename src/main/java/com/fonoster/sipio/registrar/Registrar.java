package com.fonoster.sipio.registrar;

import com.fonoster.sipio.core.model.*;
import com.fonoster.sipio.repository.AgentRepository;
import com.fonoster.sipio.repository.PeerRepository;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.utils.AuthHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Registrar {

    static final Logger logger = LogManager.getLogger(Registrar.class);

    private final Locator locator;
    private final AddressFactory addressFactory;
    private final AuthHelper authHelper = new AuthHelper();

    public Registrar(Locator locator) throws PeerUnavailableException {
        this.locator = locator;
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
    }


    public boolean register(Request r) throws Exception {
        // For some reason this references the parent object
        // to avoid I just clone it!
        Request request = (Request) r.clone();
        ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
        AuthorizationHeader authHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
        ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
        SipURI contactURI = (SipURI) contactHeader.getAddress().getURI();
        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();
        String host = fromURI.getHost();
        int expires = 0;

        if (request.getHeader(ExpiresHeader.NAME) != null) {
            expires = ((ExpiresHeader) request.getHeader(ExpiresHeader.NAME)).getExpires();
        } else {
            expires = contactHeader.getExpires();
        }

        // Get response from header
        String response = authHeader.getResponse();
        // Get user from db or file
        User user = PeerRepository.getPeer(authHeader.getUsername());

        if (user == null) {
            // Then lets check agents
            user = AgentRepository.getAgent(host, authHeader.getUsername());
        }

        if (user == null) {
            logger.warn("Could not find user or peer \"" + authHeader.getUsername() + "\"");
            return false;
        }

        if (user instanceof Peer && !isEmpty(((Peer) user).getContactAddr())) {
            if (((Peer) user).getContactAddr().contains(":")) {
                String contactAddr = ((Peer) user).getContactAddr();
                contactURI.setHost(contactAddr.split(":")[0]);
                contactURI.setPort(Integer.valueOf(contactAddr.split(":")[1]));
            } else {
                contactURI.setHost(((Peer) user).getContactAddr());
            }
        } else {
            if (isEmpty(viaHeader.getReceived())) contactURI.setHost(viaHeader.getReceived());
            if (isEmpty(viaHeader.getParameter("rport")))
                contactURI.setPort(Integer.valueOf(viaHeader.getParameter("rport")));
        }

        if (user instanceof Agent && !this.hasDomain(((Agent) user).getDomains(), host)) {
            logger.debug("User " + user.getUsername() + " does not exist within domain " + host);
            return false;
        }

        String calculatedResponse = authHelper.calculateResponse(
                user.getUsername(),
                user.getSecret(),
                authHeader.getRealm(),
                authHeader.getNonce(),
                this.getNonceCount(authHeader.getNonceCount()),
                authHeader.getCNonce(),
                authHeader.getURI().toString(),
                "REGISTER",
                authHeader.getQop()
        );

        if (calculatedResponse.equals(response)) {
            // Detect NAT
            boolean nat = (viaHeader.getHost() + viaHeader.getPort()) != (viaHeader.getReceived() + viaHeader.getParameter("rport"));

            Route route = new Route();
            route.setContactURI(contactURI);
            route.setLinkAOR(false);
            route.setThruGw(false);
            route.setSentByAddress(viaHeader.getHost());
            route.setSentByPort(viaHeader.getPort());
            route.setReceived(viaHeader.getReceived());
            route.setRport(viaHeader.getParameter("rport"));
            route.setRegisteredOn(LocalDateTime.now());
            route.setExpires(expires);
            route.setNat(nat);


            if (user instanceof Peer) {
                String peerHost = isEmpty(user.getDevice()) ? host : user.getDevice();
                SipURI addressOfRecord = this.addressFactory.createSipURI(user.getUsername(), peerHost);
                addressOfRecord.setSecure(contactURI.isSecure());
                this.locator.addEndpoint(addressOfRecord, route);
            } else {
                Agent agent = (Agent) user;
                for (String domain : agent.getDomains()) {
                    SipURI addressOfRecord = this.addressFactory.createSipURI(user.getUsername(), domain);
                    addressOfRecord.setSecure(contactURI.isSecure());
                    this.locator.addEndpoint(addressOfRecord, route);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean hasDomain(List<String> domains, String domain) {
        if (domains == null || domains.isEmpty()) return false;
        for (String d : domains) {
            if (d.equals(domain)) return true; // @todo - check === opearator converted to this.
        }
        return false;
    }


    public String getNonceCount(int d) {
        String h = Integer.toHexString(d);
        int cSize = 8 - h.toString().length();
        String nc = "";
        int cnt = 0;

        while (cSize > cnt) {
            nc += "0";
            cnt++;
        }

        return nc + h;
    }
}
