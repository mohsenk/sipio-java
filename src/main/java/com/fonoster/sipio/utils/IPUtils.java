package com.fonoster.sipio.utils;

import org.apache.commons.net.util.SubnetUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPUtils {

    private static final String ipPattern =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    static final String cidrPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([0-9]|[1-2][0-9]|3[0-2]))?$";

    public static boolean isIp(String address) {
        return Pattern.compile(ipPattern).matcher(address).matches();
    }

    public static Boolean isCidr(String cidr) {
        return cidr.matches(cidrPattern) && cidr.contains("/");
    }

    public static boolean isIpAndMask(String address) {
        String[] items = address.split("/");
        if (items.length != 2) return false;
        return isIp(items[0]) && isIp(items[1]);
    }

    public static boolean isLocalNet(List<String> localNets, String address) throws Exception {

        if (localNets == null || localNets.isEmpty()) throw new Exception("No localnets found");

        for (String localNet : localNets) {
            SubnetUtils subnetUtils = getSubnetUtils(localNet);
            if (subnetUtils.getInfo().isInRange(address)) return true;
        }
        return false;
    }

    public static SubnetUtils getSubnetUtils(String address) {
        SubnetUtils subnetUtils = null;
        if (isIp(address)) {
            subnetUtils = new SubnetUtils(address + "/31");
        } else if (isCidr(address)) {
            subnetUtils = new SubnetUtils(address);
        } else if (isIpAndMask(address)) {
            String[] s = address.split("/");
            subnetUtils = new SubnetUtils(s[0], s[1]);
        } else {
            throw new RuntimeException("Invalid rule notation. Must be IPv4 value, CIDR, or Ip/Mask notation.");
        }
        subnetUtils.setInclusiveHostCount(true);
        return subnetUtils;
    }

}
