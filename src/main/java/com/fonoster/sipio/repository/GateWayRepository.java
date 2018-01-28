package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Gateway;

import java.util.List;

public class GateWayRepository {
    public static Gateway getGateway(String gwRef) {
        for (Gateway gateway : ConfigManager.getGateways()) {
            if (gateway.getRef().equals(gwRef)) {
                return gateway;
            }
        }
        return null;
    }

    public static List<Gateway> getGateways() {
        return ConfigManager.getGateways();
    }
}
