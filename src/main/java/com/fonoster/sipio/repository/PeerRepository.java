package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Peer;

public class PeerRepository {

    public static Peer getPeer(String username) {
        for (Peer peer : ConfigManager.getPeers()) {
            if (peer.getUsername().equals(username)) {
                return peer;
            }
        }
        return null;
    }

    public static boolean peerExist(String username) {
        return getPeer(username) != null;
    }
}
