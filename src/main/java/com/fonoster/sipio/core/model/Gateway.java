package com.fonoster.sipio.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Gateway {
    String name;
    String ref;
    String host;
    String userName;
    String secret;
    String transport;
    List<String> registries = new ArrayList<>();

    public Gateway(JsonObject json) {
        this.name = json.get("metadata").getAsJsonObject().get("name").getAsString();
        this.ref = json.get("metadata").getAsJsonObject().get("ref").getAsString();
        JsonObject specJson = json.get("spec").getAsJsonObject();
        JsonObject regServiceJson = specJson.get("regService").getAsJsonObject();
        this.host = regServiceJson.get("host").getAsString();
        this.userName = regServiceJson.get("credentials").getAsJsonObject().get("username").getAsString();
        this.secret = regServiceJson.get("credentials").getAsJsonObject().get("secret").getAsString();
        this.transport = regServiceJson.get("transport").getAsString();
        if (regServiceJson.has("registries")) {
            JsonArray registriesArray = regServiceJson.get("registries").getAsJsonArray();
            for (JsonElement registryJson : registriesArray) {
                this.registries.add(registryJson.getAsString());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}
