package com.fonoster.sipio.core.acl;

import com.fonoster.sipio.core.model.Domain;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

import static jdk.nashorn.internal.objects.Global.undefined;

public class ACLUtil {

    Set<String> generalAllow;
    Set<String> generalDeny;
    List<ACLRule> rules;

    public ACLUtil(Set<String> generalAllow, Set<String> generalDeny) {
        this.generalAllow = generalAllow;
        this.generalDeny = generalDeny;
    }

    public void addRules(Set<String> allow, Set<String> deny) throws Exception {
        if (deny.isEmpty() && deny.isEmpty())
            return;

        if (!deny.isEmpty()) {
            for (String item : deny) {
                this.rules.add(new ACLRule("deny", item));
            }
        }

        if (!allow.isEmpty()) {
            for (String item : allow) {
                this.rules.add(new ACLRule("allow", item));
            }
        }
    }

    public boolean isIpAllowed(Domain domain, String calleeIp) throws Exception {
        if (domain == null) return false;

        // Is important to reset this every time
        this.rules = new java.util.ArrayList();

        if (generalAllow != null && generalDeny != null)
            this.addRules(generalAllow,generalDeny);

        if (!domain.getAllow().isEmpty() && !domain.getDeny().isEmpty()) {
            this.addRules(domain.getAllow(), domain.getDeny());
        }

        return ACLHelper.mostSpecific(this.rules, calleeIp).getAction().equals("allow");
    }
}
