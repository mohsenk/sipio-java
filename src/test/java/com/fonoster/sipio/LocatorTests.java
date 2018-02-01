package com.fonoster.sipio;

import com.fonoster.sipio.core.model.Route;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.location.SipClient;
import org.junit.Before;
import org.junit.Test;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class LocatorTests {


    AddressFactory addressFactory;
    Locator locator;

    @Before
    public void setup() throws PeerUnavailableException {
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
        this.locator = new Locator();
    }

    @Test
    public void get_peer_route_by_host() throws Exception {
        SipURI peerContactURI = addressFactory.createSipURI("ast", "192.168.1.2:5060");
        SipURI aor = addressFactory.createSipURI("7853178070", "192.168.1.2:5060");

        Route route = new Route();
        route.setLinkAOR(false);
        route.setThruGw(false);
        route.setSentByAddress("localhost");
        route.setSentByPort(5060);
        route.setReceived("remotehost");
        route.setRport(5061);
        route.setContactURI(peerContactURI);
        route.setRegisteredOn(LocalDateTime.now());
        route.setNat(false);

        locator.addEndpoint(aor, route);

        // Check individual function
        SipClient result = locator.getPeerRouteByHost(aor);
        assertTrue(result != null);

        // ... and main function
        List<SipClient> clients = locator.findEndpoint(aor);
        assertTrue(clients != null && !clients.isEmpty());
    }
}
