package com.fonoster.sipio.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Agent extends User {


    List<String> domains;

    public Agent(JsonObject json) {
        JsonObject specJson = json.get("spec").getAsJsonObject();
        this.secret = specJson.get("credentials").getAsJsonObject().get("secret").getAsString();
        this.username = specJson.get("credentials").getAsJsonObject().get("username").getAsString();
        this.domains = new ArrayList<>();
        JsonArray domainsJson = specJson.get("domains").getAsJsonArray();
        for (JsonElement domainJson : domainsJson) {
            this.domains.add(domainJson.getAsString());
        }
    }

    public Agent(String name, String username, String secret, List<String> domains) {
        this.name = name;
        this.username = username;
        this.secret = secret;
        this.domains = domains;
    }

    public Agent(String name, String username, String secret) {
        this.name = name;
        this.username = username;
        this.secret = secret;
    }


    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
}
