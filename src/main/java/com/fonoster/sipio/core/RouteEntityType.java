package com.fonoster.sipio.core;

public enum RouteEntityType {
    THRU_GW, // means that the sender or receiver is outside the Domain and therefore, route through a Gateway.
    AGENT,
    PEER,
    DID
}
