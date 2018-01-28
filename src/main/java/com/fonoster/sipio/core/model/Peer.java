package com.fonoster.sipio.core.model;

public class Peer extends User {
    String name;
    String device;
    String username;
    String secret;
    String contactAddr;

    public Peer(String name, String device, String username, String secret, String contactAddr) {
        this.name = name;
        this.device = device;
        this.username = username;
        this.secret = secret;
        this.contactAddr = contactAddr;
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
