package com.fonoster.sipio.core.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.sip.address.TelURL;
import javax.sip.address.URI;

public class DID {
    String ref;
    String gwRef;
    String city;
    String country;
    String countryISOCode;
    String telUrl;
    String aorLink;

    public DID(JsonObject json) {
        JsonObject geoInfoJson = json.get("metadata").getAsJsonObject().get("geoInfo").getAsJsonObject();
        JsonObject specJson = json.get("spec").getAsJsonObject();
        this.ref = json.get("metadata").getAsJsonObject().get("ref").getAsString();
        this.gwRef = json.get("metadata").getAsJsonObject().get("gwRef").getAsString();
        this.city = geoInfoJson.get("city").getAsString();
        this.country = geoInfoJson.get("country").getAsString();
        this.countryISOCode = geoInfoJson.get("countryISOCode").getAsString();

        this.telUrl = specJson.get("location").getAsJsonObject().get("telUrl").getAsString();
        this.aorLink = specJson.get("location").getAsJsonObject().get("aorLink").getAsString();
    }


    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getGwRef() {
        return gwRef;
    }

    public void setGwRef(String gwRef) {
        this.gwRef = gwRef;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public String getTelUrl() {
        return telUrl;
    }

    public void setTelUrl(String telUrl) {
        this.telUrl = telUrl;
    }

    public String getAorLink() {
        return aorLink;
    }

    public void setAorLink(String aorLink) {
        this.aorLink = aorLink;
    }
}
