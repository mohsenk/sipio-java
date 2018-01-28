package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Domain;

import java.util.List;

public class DomainRepository {

    public static Domain getDomain(String host) {
        for (Domain domain : getDomains()) {
            if (domain.getDomainUri().equals(host)) {
                return domain;
            }

        }
        return null;
    }

    public static List<Domain> getDomains() {
        return ConfigManager.getDomains();
    }
}
