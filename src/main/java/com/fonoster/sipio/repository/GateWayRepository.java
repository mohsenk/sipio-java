package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Gateway;

public class GateWayRepository {
    public static Gateway getGateway(String gwRef) {
        for (Gateway gateway : ConfigManager.getGateways()) {
            if (gateway.getRef().equals(gwRef)) {
                return gateway;
            }
        }
        return null;
    }
}
