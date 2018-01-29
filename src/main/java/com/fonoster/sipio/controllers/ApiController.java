package com.fonoster.sipio.controllers;


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

    @GetMapping(value = "/clients")
    public List<SipClient> getClients() {
        return locator.getClients();
    }

}
