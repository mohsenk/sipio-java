package com.fonoster.sipio.core.acl;

import java.util.List;
import java.util.Optional;

public class ACLHelper {
    public static ACLRule mostSpecific(List<ACLRule> rules, String ip) throws Exception {
        Optional<ACLRule> r = rules.stream()
                .filter(rule -> rule.hasIp(ip))
                .sorted((r1, r2) -> Long.compare(r1.getAddressCount(), r2.getAddressCount()))
                .findFirst();

        if (r.isPresent()) {
            return r.get();
        }
         return new ACLRule("allow", "0.0.0.0/0");
    }
}
