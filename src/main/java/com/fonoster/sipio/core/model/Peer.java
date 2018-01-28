package com.fonoster.sipio.core.model;

import com.google.gson.JsonObject;

public class Peer extends User {
    String name;
    String device;
    String username;
    String secret;
    String contactAddr;

    public Peer(JsonObject json) {
        JsonObject specJson = json.get("spec").getAsJsonObject();
        this.name = json.get("metadata").getAsJsonObject().get("name").getAsString();
        this.secret = specJson.get("credentials").getAsJsonObject().get("secret").getAsString();
        this.username = specJson.get("credentials").getAsJsonObject().get("username").getAsString();
        if (specJson.has("device")) {
            this.device = specJson.get("device").getAsString();
        }
        if (specJson.has("contactAddr")) {
            this.contactAddr = specJson.get("contactAddr").getAsString();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDevice() {
        return device;
    }

    @Override
    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getContactAddr() {
        return contactAddr;
    }

    public void setContactAddr(String contactAddr) {
        this.contactAddr = contactAddr;
    }
}
