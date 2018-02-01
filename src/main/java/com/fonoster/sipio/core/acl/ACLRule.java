package com.fonoster.sipio.core.acl;

import com.fonoster.sipio.utils.IPUtils;
import org.apache.commons.net.util.SubnetUtils;

public class ACLRule {

    String action;
    String net;
    SubnetUtils subnetUtils;

    public ACLRule(String action, String net) throws Exception {
        if (!action.equals("allow") && !action.equals("deny"))
            throw new Exception("Parameter action can only be 'allow' or 'deny'");

        SubnetUtils subnetUtils = IPUtils.getSubnetUtils(net);


        this.subnetUtils = subnetUtils;
        this.action = action;
        this.net = net;
    }


    public boolean hasIp(String address) {
        return this.subnetUtils.getInfo().isInRange(address);
    }

    public Long getAddressCount() {
        return this.subnetUtils.getInfo().getAddressCountLong();
    }

    public String getAction() {
        return action;
    }

    public String getNet() {
        return net;
    }
}
