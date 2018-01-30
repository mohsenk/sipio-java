package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.*;
import com.fonoster.sipio.core.model.*;
import com.fonoster.sipio.location.SipClient;
import com.fonoster.sipio.registry.GatewayConnector;
import com.fonoster.sipio.repository.*;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.registrar.Registrar;
import com.fonoster.sipio.utils.IPUtils;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;


public class RequestProcessor {


    private final RegisterHandler registerHandler;
    private final CancelHandler cancelHandler;

    private final DigestServerAuthenticationHelper dsam;
    private final Config config;
    private final Object generalACL;
    SipProvider sipProvider;
    SipStack sipStack;
    ContextStorage contextStorage;
    Locator locator;
    GatewayConnector gatewayConnector;
    MessageFactory messageFactory;
    HeaderFactory headerFactory;
    AddressFactory addressFactory;

    static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    public RequestProcessor(SipProvider sipProvider, Locator locator, GatewayConnector gatewayConnector, Registrar registrar, ContextStorage contextStorage) throws PeerUnavailableException, NoSuchAlgorithmException {
        this.sipProvider = sipProvider;
        this.sipStack = sipProvider.getSipStack();
        this.contextStorage = contextStorage;
        this.locator = locator;
        this.gatewayConnector = gatewayConnector;
        this.messageFactory = SipFactory.getInstance().createMessageFactory();
        this.headerFactory = SipFactory.getInstance().createHeaderFactory();
        this.addressFactory = SipFactory.getInstance().createAddressFactory();
        this.dsam = new DigestServerAuthenticationHelper();
        this.config = ConfigManager.getConfig();
        this.generalACL = null; // @todo fix this
        this.registerHandler = new RegisterHandler(locator, registrar);
        this.cancelHandler = new CancelHandler(sipProvider, contextStorage);


    }

    public void process(RequestEventExt event) throws Exception {
        Request requestIn = event.getRequest();
        String method = requestIn.getMethod();
        ServerTransaction serverTransaction = event.getServerTransaction();


        logger.info("New request received , method : {}", method);
        logger.info("\n" + requestIn.toString());
        // ACK does not need a transaction
        if (serverTransaction == null && !method.equals(Request.ACK)) {
            serverTransaction = this.sipProvider.getNewServerTransaction(requestIn);
        }


        if (method.equals(Request.OPTIONS)) {
            Response okResponse = this.messageFactory.createResponse(Response.OK, requestIn);
            this.sipProvider.sendResponse(okResponse);
            return;
        } else if (method.equals(Request.REGISTER)) {
            // Should we apply ACL rules here too?
            this.registerHandler.register(requestIn, serverTransaction);
            return;
        } else if (method.equals(Request.CANCEL)) {
            this.cancelHandler.cancel(requestIn, serverTransaction);
            return;
        } else {
            processInvite(event, requestIn, method, serverTransaction);
        }
    }

    public void processInvite(RequestEventExt event, Request requestIn, String method, ServerTransaction serverTransaction) throws Exception {
        FromHeader fromHeader = (FromHeader) requestIn.getHeader(FromHeader.NAME);
        SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();
        String remoteIp = event.getRemoteIpAddress();
        Request requestOut = (Request) requestIn.clone();
        RouteInfo routeInfo = new RouteInfo(requestIn);

        logger.info("routing type -> " + routeInfo.getRoutingType());

        // 1. Security check
        // This routing type is not yet supported
        if (routeInfo.getRoutingType() == RoutingType.INTER_DOMAIN_ROUTING) {
            serverTransaction.sendResponse(this.messageFactory.createResponse(Response.FORBIDDEN, requestIn));
            logger.debug("", requestIn);
            return;
        }

        if (routeInfo.getRoutingType() == RoutingType.DOMAIN_INGRESS_ROUTING) {
            if (!this.gatewayConnector.hasIp(remoteIp)) { // calling from outside that is'nt in my gateways ip
                serverTransaction.sendResponse(this.messageFactory.createResponse(Response.UNAUTHORIZED, requestIn));
                logger.warn("UNAUTHORIZED : Calling from a PSTN and it IP not defined in gateways !", requestIn);
                return;
            }

        } else {
            // Do not need to authorized ACK messages...
            if (!method.equals(Request.ACK) && !method.equals(Request.BYE) && !this.authorized(requestIn, serverTransaction)) {
                serverTransaction.sendResponse(this.messageFactory.createResponse(Response.UNAUTHORIZED, requestIn));
                logger.debug("", requestIn);
                return;
            }
        }

        SipURI addressOfRecord = (SipURI) this.getAOR(requestIn);

        // We only apply ACL rules to Domain Routing.
        if (routeInfo.getRoutingType() == RoutingType.INTRA_DOMAIN_ROUTING) { // calling in the same domain
            Domain result = DomainRepository.getDomain(addressOfRecord.getHost());
            if (result != null) {
//                    if (!new ACLUtil(new HashSet<>(), new HashSet<>()).isIpAllowed(result, remoteIp)) { // @todo - fix this
//                        serverTransaction.sendResponse(this.messageFactory.createResponse(Response.UNAUTHORIZED, requestIn));
//                        logger.debug(requestIn);
//                        return;
//                    }
            }
        }

        // 2. Decrement the max forwards value
        MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) requestOut.getHeader(MaxForwardsHeader.NAME);
        maxForwardsHeader.decrementMaxForwards();

        // 3. Determine route
        // 3.0 Peer Egress Routing (PR)
        if (routeInfo.getRoutingType() == RoutingType.PEER_EGRESS_ROUTING) {
            TelURL telUrl;

            // First look for the header "DIDRef"
            if (requestOut.getHeader("DIDRef") != null) {
                telUrl = this.addressFactory.createTelURL(((ExtensionHeader) requestOut.getHeader("DIDRef")).getValue());
            } else {
                telUrl = this.addressFactory.createTelURL(fromURI.getUser());
            }

            DID result = DIDsRepository.getDIDByTelUrl(telUrl);

            if (result == null) {
                serverTransaction.sendResponse(this.messageFactory.createResponse(Response.TEMPORARILY_UNAVAILABLE, requestIn));
                logger.debug("", requestIn);
                return;
            }

            String didRef = result.getRef();
            Route route = this.locator.getEgressRouteForPeer(addressOfRecord, didRef);

            if (route == null) {
                serverTransaction.sendResponse(this.messageFactory.createResponse(Response.TEMPORARILY_UNAVAILABLE, requestIn));
                logger.debug("", requestIn);
                return;
            }

            this.processRoute(requestIn, requestOut, route, serverTransaction);

            logger.debug("", requestOut);
            return;
        }

        // 3.1 Intra-Domain Routing(IDR), Domain Ingress Routing (DIR), & Domain Egress Routing (DER)
        List<SipClient> clients = this.locator.findEndpoint(addressOfRecord);

        if (clients == null || clients.isEmpty()) {
            serverTransaction.sendResponse(this.messageFactory.createResponse(Response.TEMPORARILY_UNAVAILABLE, requestIn));
            logger.debug("", requestIn);
            return;
        }

        for (SipClient client : clients) {
            logger.info("Send INVITE Request for client : {} ", client.getId());
            this.processRoute(requestIn, requestOut, client.getRoute(), serverTransaction);
        }

        return;
    }

    public void processRoute(Request requestIn, Request requestOut, Route route, ServerTransaction serverTransaction) throws SipException, InvalidArgumentException, ParseException {
        requestOut.setRequestURI(route.getContactURI());
        RouteHeader routeHeader = (RouteHeader) requestIn.getHeader(RouteHeader.NAME);
        ViaHeader rVia = (ViaHeader) requestIn.getHeader(ViaHeader.NAME);
        String transport = rVia.getTransport().toLowerCase();
        ListeningPoint lp = this.sipProvider.getListeningPoint(transport);
        int localPort = lp.getPort();
        String localIp = lp.getIPAddress().toString();
        String method = requestIn.getMethod();
        String rcvHost = ((SipURI) route.getContactURI()).getHost();

        logger.debug("contactURI is -> " + route.getContactURI());
        logger.debug("Behind nat -> " + route.getNat());
        logger.debug("rcvHost is -> " + rcvHost);
        logger.debug("sentByAddress -> " + route.getSentByAddress());
        logger.debug("sentByPort -> " + route.getSentByPort());
        logger.debug("received -> " + route.getReceived());
        logger.debug("rport -> " + route.getRport());

        String advertisedAddr;
        Integer advertisedPort;

        if (this.config.getExternalAddress() != null && !IPUtils.isLocalNet(route.getSentByAddress())) {
            advertisedAddr = this.config.getExternalAddress().contains(":") ? this.config.getExternalAddress().split(":")[0] : this.config.getExternalAddress();
            advertisedPort = this.config.getExternalAddress().contains(":") ? Integer.valueOf(this.config.getExternalAddress().split(":")[1]) : lp.getPort();
        } else {
            advertisedAddr = localIp;
            advertisedPort = lp.getPort();
        }

        logger.debug("advertisedAddr is -> " + advertisedAddr);
        logger.debug("advertisedPort is -> " + advertisedPort);

        // Remove route header if host is same as the proxy
        if (routeHeader != null) {
            SipURI uri = (SipURI) routeHeader.getAddress().getURI();
            String routeHeaderHost = uri.getHost();
            Integer routeHeaderPort = uri.getPort();
            if ((routeHeaderHost.equals(localIp) && routeHeaderPort.equals(localPort))
                    || ((routeHeaderHost.equals(advertisedAddr) && routeHeaderPort.equals(advertisedPort)))) {
                requestOut.removeFirst(RouteHeader.NAME);
            }
        }

        // i commented this lines for this issue : The parameter `spec.recordRoute` should not be used until further notice
        // Stay in the signaling path
//        if (this.config.spec.recordRoute) {
//            SipURI proxyURI = this.addressFactory.createSipURI(null, advertisedAddr);
//            proxyURI.setLrParam();
//            proxyURI.setPort(advertisedPort);
//            Address proxyAddress = this.addressFactory.createAddress(proxyURI);
//            RecordRouteHeader recordRouteHeader = this.headerFactory.createRecordRouteHeader(proxyAddress);
//            requestOut.addHeader(recordRouteHeader);
//        }

        // Request RPort to enable Symmetric Response in accordance with RFC 3581 and RFC 6314
        ViaHeader viaHeader = this.headerFactory.createViaHeader(advertisedAddr, advertisedPort, transport, null);
        viaHeader.setRPort();
        requestOut.addFirst(viaHeader);

        if (route.isThruGw()) {
            FromHeader fromHeader = (FromHeader) requestIn.getHeader(FromHeader.NAME);
            ToHeader toHeader = (ToHeader) requestIn.getHeader(ToHeader.NAME);
            Header gwRefHeader = this.headerFactory.createHeader("GwRef", route.getGwRef());
            Header remotePartyIdHeader = this.headerFactory
                    .createHeader("Remote-Party-ID", "<sip:" + route.getDID() + "@" + route.getGwHost() + ">;screen=yes;party=calling");

            String from = "sip:" + route.getGwUsername() + "@" + route.getGwHost();
            String to = "sip:" + Pattern.compile("sips?:(.*)@(.*)").split(toHeader.getAddress().toString())[1] + "@" + route.getGwHost();

            // This might not work with all provider
            Address fromAddress = this.addressFactory.createAddress(from);
            Address toAddress = this.addressFactory.createAddress(to);

            fromHeader.setAddress(fromAddress);
            toHeader.setAddress(toAddress);

            requestOut.setHeader(gwRefHeader);
            requestOut.setHeader(fromHeader);
            requestOut.setHeader(toHeader);
            requestOut.setHeader(remotePartyIdHeader);
        }

        // Warning: Not yet test :(
        requestOut.removeHeader("Proxy-Authorization");

        // Does not need a transaction
        if (method.equals(Request.ACK)) {
            this.sipProvider.sendRequest(requestOut);
        } else {
            try {
                // The request must be cloned or the stack will not fork the call
                ClientTransaction clientTransaction = this.sipProvider.getNewClientTransaction((Request) requestOut.clone());
                clientTransaction.sendRequest();
                logger.info("Send invite request to peer : {}", requestOut);

                // Transaction context
                Context context = new Context();
                context.setClientTransaction(clientTransaction);
                context.setServerTransaction(serverTransaction);
                context.setMethod(method);
                context.setRequestIn(requestIn);
                context.setRequestOut(requestOut);
                this.contextStorage.saveContext(context);
            } catch (Exception e) {
                if (e instanceof java.net.ConnectException) {
                    logger.error("Connection refused. Please see: https://docs.oracle.com/javase/7/docs/api/java/net/ConnectException.html");
                } else if (e instanceof java.net.NoRouteToHostException) {
                    logger.error("No route to host. Please see: https://docs.oracle.com/javase/7/docs/api/java/net/NoRouteToHostException.html");
                } else {
                    logger.error(e.getMessage());
                }
            }
        }

        logger.debug("", requestOut);
    }

    public boolean authorized(Request request, ServerTransaction serverTransaction) throws ParseException, SipException, InvalidArgumentException {
        ProxyAuthorizationHeader authHeader = (ProxyAuthorizationHeader) request.getHeader(ProxyAuthorizationHeader.NAME);
        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();

        if (authHeader == null) {
            Response challengeResponse = this.messageFactory.createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, request);
            this.dsam.generateChallenge(this.headerFactory, challengeResponse, "sipio");
            serverTransaction.sendResponse(challengeResponse);
            logger.debug("", request);
            return false;
        }

        User user = PeerRepository.getPeer(authHeader.getUsername());

        if (user == null) {
            // This is also a security check. The user in the authentication must exist for the "fromURI.getHost()" domain
            Agent agent = AgentRepository.getAgent(fromURI.getHost(), authHeader.getUsername());
            if (agent != null) {
                user = agent;
            }
        }

        if (!this.dsam.doAuthenticatePlainTextPassword(request, user.getSecret())) {

            Response challengeResponse = this.messageFactory.createResponse(Response.PROXY_AUTHENTICATION_REQUIRED, request);
            this.dsam.generateChallenge(this.headerFactory, challengeResponse, "sipio");
            serverTransaction.sendResponse(challengeResponse);
            logger.debug("", request);
            return false;
        }

        return user != null;
    }


    /**
     * Discover DIDs sent via a non-standard headerRouًخع
     * The header must be added at config.spec.addressInfo[*]
     * If the such header is present then overwrite the AOR
     */
    public URI getAOR(Request request) {
        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);

//        if (!!this.config.spec.addressInfo) {
//            this.config.spec.addressInfo.forEach(function(info) {
//                if (!!request.getHeader(info)) {
//                    return this.addressFactory.createTelURL(request.getHeader(info).getValue());
//                }
//            })
//        }

        return toHeader.getAddress().getURI();
    }
}
