package com.fonoster.sipio.core;

public class Transport {
    String address;
    Integer port;
    String protocol;

    public Transport(String address, Integer port, String protocol) {
        this.address = address;
        this.port = port;
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
