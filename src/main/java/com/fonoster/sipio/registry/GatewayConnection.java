package com.fonoster.sipio.registry;

import java.time.LocalDateTime;

public class GatewayConnection {
    String username;
    String host;
    String ip;
    Integer expires;
    LocalDateTime registeredOn;

    public GatewayConnection(String username, String host, String ip, Integer expires) {
        this.username = username;
        this.host = host;
        this.ip = ip;
        this.expires = expires;
        this.registeredOn = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getExpires() {
        return expires;
    }

    public void setExpires(Integer expires) {
        this.expires = expires;
    }

    public LocalDateTime getRegisteredOn() {
        return registeredOn;
    }

    public void setRegisteredOn(LocalDateTime registeredOn) {
        this.registeredOn = registeredOn;
    }
}
