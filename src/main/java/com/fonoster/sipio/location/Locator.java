package com.fonoster.sipio.location;

import com.fonoster.sipio.core.model.DID;
import com.fonoster.sipio.core.model.Domain;
import com.fonoster.sipio.core.model.Gateway;
import com.fonoster.sipio.core.model.Route;
import com.fonoster.sipio.repository.DIDsRepository;
import com.fonoster.sipio.repository.DomainRepository;
import com.fonoster.sipio.repository.GateWayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import javax.sip.address.URI;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class Locator {

    static final Logger logger = LoggerFactory.getLogger(Locator.class);
    private final AddressFactory addressFactory;
    private List<SipClient> clients;
    private Integer checkExpiresTime;

    public List<SipClient> getClients() {
        return clients;
    }

    public List<SipClient> getClient(String aor) {
        logger.info("Locating user with AOR : {}", aor);
        List<SipClient> results = new ArrayList<>();
        for (SipClient client : clients) {
            if (client.getAor().equals(aor)) {
                results.add(client);
            }
        }
        return results;
    }

    public Locator(Integer checkExpiresTime) throws PeerUnavailableException {
        this.checkExpiresTime = checkExpiresTime;
        this.clients = new ArrayList<>();
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
    }

    public Locator() throws PeerUnavailableException {
        this(1);
    }


    public void removeEndpoint(URI addressOfRecord) throws Exception {
        this.aorAsString(addressOfRecord);
    }

    private String aorAsString(Object addressOfRecord) throws Exception {
        if (addressOfRecord instanceof javax.sip.address.TelURL) {
            return "tel:" + ((TelURL) addressOfRecord).getPhoneNumber();
        } else if (addressOfRecord instanceof javax.sip.address.SipURI) {
            if (((SipURI) addressOfRecord).isSecure()) {
                return "sips:" + ((SipURI) addressOfRecord).getUser() + "@" + ((SipURI) addressOfRecord).getHost();
            } else {
                return "sip:" + ((SipURI) addressOfRecord).getUser() + "@" + ((SipURI) addressOfRecord).getHost();
            }
        } else {
            if (Pattern.matches("/sips?:.*@.*/", addressOfRecord.toString()) || Pattern.matches("/tel:\\d+/", addressOfRecord.toString())) {
                return addressOfRecord.toString();
            }
            logger.error("Invalid AOR: " + addressOfRecord);
        }

        throw new Exception("Invalid AOR: " + addressOfRecord);
    }

    public void addEndpoint(URI addressOfRecord, Route route) throws Exception {
        List<SipClient> result = this.findEndpoint(addressOfRecord);

//        Object routes;
//
//        // ThruGw is not available in db. We obtain that from api
//        if (result != null && result instanceof Route && !((Route) result).isThruGw()) {
//            routes = result;
//        } else {
//            routes = new HashMap();
//        }

        // Not using aorAsString because we need to consider the port, etc.
        String routeKey = route.getContactURI().toString();
        // See NOTE #1
        this.clients.add(new SipClient(this.aorAsString(addressOfRecord), routeKey, route));
    }


    public List<SipClient> findEndpoint(URI addressOfRecord) throws Exception {
        if (addressOfRecord instanceof javax.sip.address.TelURL) {
            DID did = DIDsRepository.getDIDByTelUrl((TelURL) addressOfRecord);
            if (did != null) {
                List<SipClient> route = getClient(this.aorAsString(did.getAorLink()));

                if (route != null) {
                    return route;
                }
            }
        } else if (addressOfRecord instanceof javax.sip.address.SipURI) {

            // First just check the db for such addressOfRecord
            List<SipClient> routes = getClient(this.aorAsString(addressOfRecord));

            if (routes != null) {
                return routes;
            }

            // Then search for a DID
            try {
                TelURL telUrl = this.addressFactory.createTelURL(((SipURI) addressOfRecord).getUser());
                DID did = DIDsRepository.getDIDByTelUrl(telUrl);

                if (did != null) {
                    List<SipClient> route = getClient(this.aorAsString(did.getAorLink()));

                    if (route != null) {
                        return route;
                    }
                }
            } catch (Exception e) {
                // Ignore error
                logger.error("", e);
            }

            // Endpoint can only be reach thru a gateway
            Route route = this.getEgressRouteForAOR((SipURI) addressOfRecord);
            return Arrays.asList(new SipClient(this.aorAsString(addressOfRecord), "", route));
        }
        return null;
    }

    public void removeEndpoint(URI addressOfRecord, URI contactURI) throws Exception {
        String aor = this.aorAsString(addressOfRecord);
        logger.info("Remove client with URI : {} and URI : {}", addressOfRecord, contactURI);
        // Remove all bindings
        if (contactURI == null) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getId().equals(aor)) {
                    clients.remove(i);
                    i--;
                }
            }
            return;
        }
        // Not using aorAsString because we need to consider the port, etc.
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId().equals(contactURI.toString())) {
                clients.remove(i);
                i--;
            }
        }
    }

    public Route getEgressRouteForAOR(SipURI addressOfRecord) throws Exception {
        List<Domain> domains = DomainRepository.getDomains();
        if (domains == null) {
            return null;
        }
        for (Domain domain : domains) {
            if (domain.getRule() == null) continue;
            // Get DID and Gateway info
            DID did = DIDsRepository.getDID(domain.getDidRef());
            if (did == null) continue;
            Gateway gateway = GateWayRepository.getGateway(did.getGwRef());
            if (gateway == null) continue;
            String gwHost = gateway.getHost();
            String gwUsername = gateway.getUserName();
            String gwRef = gateway.getRef();
            String egressRule = domain.getRule();
            String pattern = "sip:" + egressRule + "@" + domain.getDomainUri();

            if (pattern.matches(addressOfRecord.toString())) {
                SipURI contactURI = addressFactory.createSipURI(addressOfRecord.getUser(), gwHost);
                contactURI.setSecure(addressOfRecord.isSecure());
                Route route = new Route();
                route.setLinkAOR(false);
                route.setThruGw(true);
                route.setRule(egressRule);
                route.setGwUsername(gwUsername);
                route.setGwRef(gwRef);
                route.setGwHost(gwHost);
                route.setDidRef(did.getRef());
                route.setDID(did.getTelUrl().split(":")[0]);
                route.setContactURI(contactURI);
                return route;
            }
        }
        return null;
    }

    // @todo - need to change Route model
    public Route getEgressRouteForPeer(SipURI addressOfRecord, String didRef) throws ParseException {
        AddressFactory addressFactory = this.addressFactory;
        DID did = DIDsRepository.getDID(didRef);
        if (did != null) {
            Gateway gateway = GateWayRepository.getGateway(did.getGwRef());
            if (gateway != null) {
                SipURI contactURI = addressFactory.createSipURI(addressOfRecord.getUser(), gateway.getHost());
                Route route = new Route();
                route.setThruGw(true);
                route.setLinkAOR(false);
                route.setGwUsername(gateway.getUserName());
                route.setGwRef(gateway.getRef());
                route.setDID(did.getTelUrl().split(":")[1]);
                route.setContactURI(contactURI);
                return route;
            }
        }
        return null;
    }


    public void start() {
        logger.info("Starting Location service");
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < clients.size(); i++) {
                    Route route = clients.get(i).getRoute();
                    long elapsed = Duration.between(LocalDateTime.now(), route.getRegisteredOn()).getSeconds();
                    if ((route.getExpires() - elapsed) <= 0) {
                        clients.remove(i);
                        i--;
                        logger.info("Expired user , remove {} from DB", route.getContactURI().toString());
                    }
                }
            }
        }, 5000, this.checkExpiresTime * 60 * 1000);
    }

    public void stop() {


    }
}
