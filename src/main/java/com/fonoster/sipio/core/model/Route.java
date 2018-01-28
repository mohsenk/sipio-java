package com.fonoster.sipio.core.model;

import javax.sip.address.URI;
import java.time.LocalDateTime;

public class Route {

    boolean isLinkAOR;
    boolean thruGw;

    String sentByAddress;
    Integer sentByPort;
    String received;
    String rport;
    URI contactURI;
    LocalDateTime registeredOn;
    Integer expires;
    Boolean nat;

    String rule;
    String gwHost;
    String gwRef;
    String didRef;
    String gwUsername;

    String did;

    public String getDID() {
        return did;
    }

    public void setDID(String did) {
        this.did = did;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getGwHost() {
        return gwHost;
    }

    public void setGwHost(String gwHost) {
        this.gwHost = gwHost;
    }

    public String getGwRef() {
        return gwRef;
    }

    public void setGwRef(String gwRef) {
        this.gwRef = gwRef;
    }

    public String getDidRef() {
        return didRef;
    }

    public void setDidRef(String didRef) {
        this.didRef = didRef;
    }

    public String getGwUsername() {
        return gwUsername;
    }

    public void setGwUsername(String gwUsername) {
        this.gwUsername = gwUsername;
    }

    public boolean isLinkAOR() {
        return isLinkAOR;
    }

    public void setLinkAOR(boolean linkAOR) {
        isLinkAOR = linkAOR;
    }

    public boolean isThruGw() {
        return thruGw;
    }

    public void setThruGw(boolean thruGw) {
        this.thruGw = thruGw;
    }

    public String getSentByAddress() {
        return sentByAddress;
    }

    public void setSentByAddress(String sentByAddress) {
        this.sentByAddress = sentByAddress;
    }

    public Integer getSentByPort() {
        return sentByPort;
    }

    public void setSentByPort(Integer sentByPort) {
        this.sentByPort = sentByPort;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public String getRport() {
        return rport;
    }

    public void setRport(String rport) {
        this.rport = rport;
    }

    public URI getContactURI() {
        return contactURI;
    }

    public void setContactURI(URI contactURI) {
        this.contactURI = contactURI;
    }

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }

    public Integer getExpires() {
        return expires;
    }

    public void setExpires(Integer expires) {
        this.expires = expires;
    }

    public Boolean getNat() {
        return nat;
    }

    public void setNat(Boolean nat) {
        this.nat = nat;
    }
}
