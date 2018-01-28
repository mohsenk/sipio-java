package com.fonoster.sipio.core.model;

import java.util.List;

public class Agent extends User {


    List<String> domains;

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
