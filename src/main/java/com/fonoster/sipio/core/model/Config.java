package com.fonoster.sipio.core.model;

import com.fonoster.sipio.core.Transport;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Config {

    String userAgent;
    String keyStore;
    String trustStore;
    String keyStorePassword;
    String keyStoreType;
    Boolean sslDebugging;
    List<Transport> transport;
    String restUserName;
    String restSecret;
    Integer traceLevel;
    String externalAddress;
    String authType;
    List<String> protocols = null;

    String bindAddr;


    public List<String> getProtocols() {
        return protocols;
    }

    public String getAuthType() {
        return authType;
    }

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public Config(JsonObject json) throws UnknownHostException {

        this.bindAddr = InetAddress.getLocalHost().getHostAddress();
        JsonObject specJson = json.get("spec").getAsJsonObject();
        JsonObject metaDataJson = json.get("metadata").getAsJsonObject();
        if (metaDataJson.has("userAgent")) {
            this.userAgent = metaDataJson.get("userAgent").getAsString();
        }
        if (specJson.has("externAddr")) {
            this.externalAddress = specJson.get("externAddr").getAsString();
        }
        JsonObject securityContextJson = specJson.get("securityContext").getAsJsonObject();
        this.keyStore = securityContextJson.get("keyStore").getAsString();
        this.trustStore = securityContextJson.get("trustStore").getAsString();
        this.keyStorePassword = securityContextJson.get("keyStorePassword").getAsString();
        this.keyStoreType = securityContextJson.get("keyStoreType").getAsString();
        this.sslDebugging = securityContextJson.get("debugging").getAsBoolean();
        this.authType = "Disabled";
        this.protocols = Arrays.asList("SSLv3", "TLSv1.2", "TLSv1.1", "TLSv1");
        this.transport = new ArrayList<>();
        JsonArray transportsJson = specJson.get("transport").getAsJsonArray();
        for (JsonElement transportJson : transportsJson) {
            JsonObject transport = transportJson.getAsJsonObject();
            this.transport.add(new Transport(bindAddr, transport.get("port").getAsInt(), transport.get("protocol").getAsString()));
        }

        this.traceLevel = specJson.get("logging").getAsJsonObject().get("traceLevel").getAsInt();

    }

    public String getExternalAddress() {
        return externalAddress;
    }

    public void setExternalAddress(String externalAddress) {
        this.externalAddress = externalAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public Boolean getSSLDebugging() {
        return sslDebugging;
    }

    public void setSSLDebugging(Boolean sslDebugging) {
        this.sslDebugging = sslDebugging;
    }

    public List<Transport> getTransport() {
        return transport;
    }

    public void setTransport(List<Transport> transport) {
        this.transport = transport;
    }

    public String getRestUserName() {
        return restUserName;
    }

    public void setRestUserName(String restUserName) {
        this.restUserName = restUserName;
    }

    public String getRestSecret() {
        return restSecret;
    }

    public void setRestSecret(String restSecret) {
        this.restSecret = restSecret;
    }

    public Integer getTraceLevel() {
        return traceLevel;
    }

    public void setTraceLevel(Integer traceLevel) {
        this.traceLevel = traceLevel;
    }
}
