package com.fonoster.sipio.core.handlers;

import com.fonoster.sipio.core.RouteEntityType;
import com.fonoster.sipio.core.RoutingType;
import com.fonoster.sipio.repository.*;
import com.fonoster.sipio.utils.StringUtils;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import java.text.ParseException;

public class RouteInfo {

    private final String callerUser;
    private final String callerHost;
    private final String calleeUser;
    private final String calleeHost;

    Request request;
    AddressFactory addressFactory;

    public RouteInfo(Request request) throws PeerUnavailableException {


        FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
        this.addressFactory = SipFactory.getInstance().createAddressFactory();

        this.request = request;

        SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();
        SipURI toURI = (SipURI) toHeader.getAddress().getURI();
        this.callerUser = fromURI.getUser();
        this.callerHost = fromURI.getHost();
        this.calleeUser = toURI.getUser();
        this.calleeHost = toURI.getHost();


    }

    public RoutingType getRoutingType() throws ParseException {
        if (this.getCallerType() == RouteEntityType.AGENT && this.getCalleeType() == RouteEntityType.AGENT && this.isSameDomain()) return RoutingType.INTRA_DOMAIN_ROUTING;
        if (this.getCallerType() == RouteEntityType.AGENT && this.getCalleeType() == RouteEntityType.PEER && this.isSameDomain()) return RoutingType.INTRA_DOMAIN_ROUTING;
        if (this.getCallerType() == RouteEntityType.PEER && this.getCalleeType() == RouteEntityType.AGENT && this.isSameDomain()) return RoutingType.INTRA_DOMAIN_ROUTING;

        if (this.getCallerType() == RouteEntityType.AGENT && this.getCalleeType() == RouteEntityType.AGENT && !this.isSameDomain()) return RoutingType.INTER_DOMAIN_ROUTING;
        if (this.getCallerType() == RouteEntityType.AGENT && this.getCalleeType() == RouteEntityType.PEER && !this.isSameDomain()) return RoutingType.INTER_DOMAIN_ROUTING;
        if (this.getCallerType() == RouteEntityType.PEER && this.getCalleeType() == RouteEntityType.AGENT && !this.isSameDomain()) return RoutingType.INTER_DOMAIN_ROUTING;

        if (this.getCallerType() == RouteEntityType.AGENT && this.getCalleeType() == RouteEntityType.THRU_GW) return RoutingType.DOMAIN_EGRESS_ROUTING;
        if (this.getCallerType() == RouteEntityType.THRU_GW && this.getCalleeType() == RouteEntityType.DID) return RoutingType.DOMAIN_INGRESS_ROUTING;

        // This is consider PEER_EGRESS_ROUTING because peers are the only one allow to overwrite the FromHeader.
        if (this.getCallerType() == RouteEntityType.DID && this.getCalleeType() == RouteEntityType.THRU_GW) return RoutingType.PEER_EGRESS_ROUTING;
        if (this.getCallerType() == RouteEntityType.PEER && this.getCalleeType() == RouteEntityType.THRU_GW) return RoutingType.PEER_EGRESS_ROUTING;

        return RoutingType.UNKNOWN;
    }

    public RouteEntityType getCallerType() throws ParseException {
        if (PeerRepository.peerExist(this.callerUser)) return RouteEntityType.PEER;
        if (AgentRepository.agentExist(this.callerHost, this.callerUser)) return RouteEntityType.AGENT;
        if (StringUtils.isNumeric(this.callerUser)) {
            TelURL telUrl = this.addressFactory.createTelURL(this.callerUser);
            if (DIDsRepository.didExistByTelUrl(telUrl)) return RouteEntityType.DID;
        }
        if (AgentRepository.agentExist(this.callerHost, this.callerUser)) return RouteEntityType.AGENT;
        return RouteEntityType.THRU_GW;
    }

    public RouteEntityType getCalleeType() throws ParseException {
        if (PeerRepository.peerExist(this.calleeUser)) return RouteEntityType.PEER;
        if (AgentRepository.agentExist(this.calleeHost, this.calleeUser)) return RouteEntityType.AGENT;
        if (StringUtils.isNumeric(this.calleeUser)) {
            TelURL telUrl = this.addressFactory.createTelURL(this.calleeUser);
            if (DIDsRepository.didExistByTelUrl(telUrl)) return RouteEntityType.DID;
        }
        return RouteEntityType.THRU_GW;
    }

    public boolean isSameDomain(){
        return this.callerHost.equals(this.calleeHost);
    }


}
