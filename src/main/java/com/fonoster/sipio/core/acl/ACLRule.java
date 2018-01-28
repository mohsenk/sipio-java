package com.fonoster.sipio.core.acl;

import org.apache.commons.net.util.SubnetUtils;

public class ACLRule {
    static final String ipPattern = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    static final String cidrPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([0-9]|[1-2][0-9]|3[0-2]))?$";

    String action;
    String net;
    SubnetUtils subnetUtils;

    public ACLRule(String action, String net) throws Exception {
        if (!action.equals("allow") && !action.equals("deny"))
            throw new Exception("Parameter action can only be 'allow' or 'deny'");

        SubnetUtils subnetUtils;

        if (this.isIp(net)) {
            subnetUtils = new SubnetUtils(net + "/31");
        } else if (this.isCidr(net)) {
            subnetUtils = new SubnetUtils(net);
        } else if (this.isIpAndMask(net)) {
            String[] s = net.split("/");
            subnetUtils = new SubnetUtils(s[0], s[1]);
        } else {
            throw new java.lang.RuntimeException("Invalid rule notation. Must be IPv4 value, CIDR, or Ip/Mask notation.");
        }
        subnetUtils.setInclusiveHostCount(true);


        this.subnetUtils = subnetUtils;
        this.action = action;
        this.net = net;
    }

    public boolean isIp(String address) {
        return ipPattern.matches(ipPattern);
    }

    public boolean isCidr(String address) {
        return cidrPattern.matches(address) && address.contains("/");
    }

    public boolean isIpAndMask(String address) {
        String[] items = address.split("/");
        if (items.length != 2) return false;
        return isIp(items[0]) && isIp(items[1]);
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
