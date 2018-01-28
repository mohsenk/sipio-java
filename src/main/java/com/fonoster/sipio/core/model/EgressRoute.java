package com.fonoster.sipio.core.model;

import javax.sip.address.SipURI;

public class EgressRoute {

    Boolean isLinkAOR;
    Boolean thruGw;
    String rule;
    String username;
    String ref;
    String host;
    String didRef;
    String did;
    SipURI contactURI;


    public Boolean getLinkAOR() {
        return isLinkAOR;
    }

    public void setLinkAOR(Boolean linkAOR) {
        isLinkAOR = linkAOR;
    }

    public Boolean getThruGw() {
        return thruGw;
    }

    public void setThruGw(Boolean thruGw) {
        this.thruGw = thruGw;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDidRef() {
        return didRef;
    }

    public void setDidRef(String didRef) {
        this.didRef = didRef;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public SipURI getContactURI() {
        return contactURI;
    }

    public void setContactURI(SipURI contactURI) {
        this.contactURI = contactURI;
    }
}
