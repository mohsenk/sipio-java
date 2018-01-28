package com.fonoster.sipio.core.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public class Domain {
    String name;
    String domainUri;

    String rule;
    String didRef;

    Set<String> allow;
    Set<String> deny;

    public Set<String> getAllow() {
        return allow;
    }

    public Set<String> getDeny() {
        return deny;
    }

    public void setAllow(Set<String> allow) {
        this.allow = allow;
    }

    public void setDeny(Set<String> deny) {
        this.deny = deny;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainUri() {
        return domainUri;
    }

    public void setDomainUri(String domainUri) {
        this.domainUri = domainUri;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDidRef() {
        return didRef;
    }

    public void setDidRef(String didRef) {
        this.didRef = didRef;
    }
}

