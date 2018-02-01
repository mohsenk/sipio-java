package com.fonoster.sipio;

import com.fonoster.sipio.utils.IPUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestIPUtils {

    @Test
    public void testLocalNets() throws Exception {
        List<String> localNets = Arrays.asList("192.168.1.2", "10.88.1.0/255.255.255.0", "192.168.0.1/28");

        assertTrue(IPUtils.isLocalNet(localNets,"192.168.1.2"));
        assertTrue(IPUtils.isLocalNet(localNets,"10.88.1.34"));
        assertTrue(IPUtils.isLocalNet(localNets,"192.168.0.14"));
        assertFalse(IPUtils.isLocalNet(localNets,"35.196.78.166"));
    }
}

