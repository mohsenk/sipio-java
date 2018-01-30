package com.fonoster.sipio.rest.controllers;


import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.*;
import com.fonoster.sipio.location.Locator;
import com.fonoster.sipio.location.SipClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final Locator locator;

    public ApiController(Locator locator) {
        this.locator = locator;
    }


    @RequestMapping(value = "/config")
    Config getConfig() {
        return ConfigManager.getConfig();
    }

    @GetMapping(value = "/clients")
    List<SipClient> getClients() {
        return locator.getClients();
    }

    @GetMapping(value = "/gateways")
    List<Gateway> getGateways() {
        return ConfigManager.getGateways();
    }

    @GetMapping(value = "/agents")
    List<Agent> getAgents() {
        return ConfigManager.getAgents();
    }

    @GetMapping("/peers")
    List<Peer> getPeers() {
        return ConfigManager.getPeers();
    }

    @GetMapping("/dids")
    List<DID> getDIDs() {
        return ConfigManager.getDIDs();
    }

    @GetMapping("/domains")
    List<Domain> getDomains() {
        return ConfigManager.getDomains();
    }


}
