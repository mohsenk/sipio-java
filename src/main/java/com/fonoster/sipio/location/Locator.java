package com.fonoster.sipio.location;

import com.fonoster.sipio.core.model.*;
import com.fonoster.sipio.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import javax.sip.address.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Locator {

    static final Logger logger = LogManager.getLogger(Locator.class);
    private final AddressFactory addressFactory;
    private Map<String, Object> db;
    private Integer checkExpiresTime;


    public Locator( Integer checkExpiresTime) throws PeerUnavailableException {
        this.checkExpiresTime = checkExpiresTime;
        this.db = new HashMap();
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
    }

    public Locator() throws PeerUnavailableException {
        this(0);
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
        Object result = this.findEndpoint(addressOfRecord);

        Object routes;

        // ThruGw is not available in db. We obtain that from api
        if (result != null && !((Route) result).isThruGw()) {
            routes = result;
        } else {
            routes = new HashMap();
        }

        // Not using aorAsString because we need to consider the port, etc.
        String routeKey = route.getContactURI().toString();
        ((HashMap) routes).put(routeKey, route);

        // See NOTE #1
        this.db.put(this.aorAsString(addressOfRecord), routes);
    }


    public Object findEndpoint(URI addressOfRecord) throws Exception {
        if (addressOfRecord instanceof javax.sip.address.TelURL) {
            DID did = DIDsRepository.getDIDByTelUrl((TelURL) addressOfRecord);
            if (did != null) {
                Object route = this.db.get(this.aorAsString(did.getAorLink()));

                if (route != null) {
                    return route;
                }
            }
        } else if (addressOfRecord instanceof javax.sip.address.SipURI) {

            // First just check the db for such addressOfRecord
            Object routes = this.db.get(this.aorAsString(addressOfRecord));

            if (routes != null) {
                return routes;
            }

            // Then search for a DID
            try {
                TelURL telUrl = this.addressFactory.createTelURL(((SipURI) addressOfRecord).getUser());
                DID did = DIDsRepository.getDIDByTelUrl(telUrl);

                if (did != null) {
                    Object route = this.db.get(this.aorAsString(did.getAorLink()));

                    if (route != null) {
                        return route;
                    }
                }
            } catch (Exception e) {
                // Ignore error
                logger.error("",e);
            }

            // Endpoint can only be reach thru a gateway
            Route route = this.getEgressRouteForAOR((SipURI) addressOfRecord);

            return route;
        }
        return null;
    }

    public void removeEndpoint(URI addressOfRecord, URI contactURI) throws Exception {
        String aor = this.aorAsString(addressOfRecord);
        // Remove all bindings
        if (contactURI == null) {
            this.db.remove(aor);
            return;
        }
        // Not using aorAsString because we need to consider the port, etc.
        ((HashMap)this.db.get(aor)).remove(contactURI.toString());

        if (((HashMap) this.db.get(aor)).isEmpty()) this.db.remove(aor);
    }

    public Route getEgressRouteForAOR(SipURI addressOfRecord) throws Exception {
        if (!(addressOfRecord instanceof javax.sip.address.SipURI))
            throw new Exception("AOR must be instance of javax.sip.address.SipURI");


        List<Domain> domains = DomainRepository.getDomains();

        Route route;

        if (domains != null) {

            for (Domain domain : domains) {
                if (domain.getRule() != null) {
                    // Get DID and Gateway info
                    DID did = DIDsRepository.getDID(domain.getDidRef());

                    if (did != null) {
                        Gateway gateway = GateWayRepository.getGateway(did.getGwRef());

                        if (gateway != null) {
                            String gwHost = gateway.getHost();
                            String gwUsername = gateway.getUserName();
                            String gwRef = gateway.getRef();
                            String egressRule = domain.getRule();
                            String pattern = "sip:" + egressRule + "@" + domain.getDomainUri();

                            if (pattern.matches(addressOfRecord.toString())) {
                                SipURI contactURI = addressFactory.createSipURI(addressOfRecord.getUser(), gwHost);
                                contactURI.setSecure(addressOfRecord.isSecure());
                                route = new Route();
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
                    }
                }
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
        // @todo - wrote this.
        //Map<String, Map> locDB = this.db;
//
//        let unbindExpiredTask = new java.util.TimerTask() {
//            @Override
//            public void run() {
//                for (String key : locDB.keySet()) {
//                    Map routes = locDB.get(key);
//                    while (i.hasNext()) {
//                        Set i = routes.keySet();
//                        const route = i.next()
//                        const elapsed = (Date.now() - route.registeredOn) / 1000
//                        if ((route.expires - elapsed) <= 0) {
//                            i.remove()
//                        }
//
//                        if (routes.size() == 0) e.remove()
//                    }
//                }
//                while (e.hasNext()) {
//                    let routes = e.next()
//
//
//
//                }
//            }
//        };

        //     new java.util.Timer().schedule(unbindExpiredTask, 5000, this.checkExpiresTime * 60 * 1000)
    }

    public void stop() {


    }
}
