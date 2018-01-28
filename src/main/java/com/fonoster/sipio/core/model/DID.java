package com.fonoster.sipio.core.model;

import javax.sip.address.TelURL;
import javax.sip.address.URI;

public class DID {
    String ref;
    String gwRef;
    String city;
    String country;
    String countryISOCode;
    TelURL telUrl;
    URI aorLink;

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

    public TelURL getTelUrl() {
        return telUrl;
    }

    public void setTelUrl(TelURL telUrl) {
        this.telUrl = telUrl;
    }

    public URI getAorLink() {
        return aorLink;
    }

    public void setAorLink(URI aorLink) {
        this.aorLink = aorLink;
    }
}
