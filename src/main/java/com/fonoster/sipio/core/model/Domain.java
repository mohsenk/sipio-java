package com.fonoster.sipio.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Domain {
    String name;
    String domainUri;

    String rule;
    String didRef;

    Set<String> allow = new HashSet<>();
    Set<String> deny = new HashSet<>();

    public Domain(JsonObject json) {
        JsonObject specJson = json.get("spec").getAsJsonObject();
        JsonObject contextJson = specJson.get("context").getAsJsonObject();
        this.name = json.get("metadata").getAsJsonObject().get("name").getAsString();
        this.domainUri = contextJson.get("domainUri").getAsString();
        if (contextJson.has("egressPolicy")) {
            this.rule = contextJson.get("egressPolicy").getAsJsonObject().get("rule").getAsString();
            this.didRef = contextJson.get("egressPolicy").getAsJsonObject().get("didRef").getAsString();
        }
        if (contextJson.has("accessControlList")) {
            JsonArray allowsJson = contextJson.get("accessControlList").getAsJsonObject().get("allow").getAsJsonArray();
            for (JsonElement allowJson : allowsJson) {
                this.allow.add(allowJson.getAsString());
            }

            JsonArray denysJson = contextJson.get("accessControlList").getAsJsonObject().get("deny").getAsJsonArray();
            for (JsonElement denyJson : denysJson) {
                this.deny.add(denyJson.getAsString());
            }
        }
    }

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

