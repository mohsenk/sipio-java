package com.fonoster.sipio.location;

import com.fonoster.sipio.core.model.Route;
import com.google.gson.Gson;

public class SipClient {

    String aor;
    String id;
    Route route;

    public SipClient(String aor, String id, Route route) {
        this.aor = aor;
        this.id = id;
        this.route = route;
    }

    public String getAor() {
        return aor;
    }

    public String getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
