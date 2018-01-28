package com.fonoster.sipio.core;

import com.fonoster.sipio.core.model.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.core.util.IOUtils;
import org.json.JSONObject;
import spark.utils.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    static List<Agent> agents;
    static List<Domain> domains;
    static List<Gateway> gateways;
    static List<Peer> peers;
    static List<DID> dids;
    static Config config;

    public static Config getConfig() {
        return config;
    }

    public static void load() throws IOException {
        JsonElement json = new ResourceUtil().getJson("config/config.yml");
        config = new Config(json.getAsJsonObject());

        agents = new ArrayList<>();
        domains = new ArrayList<>();
        gateways = new ArrayList<>();
        peers = new ArrayList<>();
        dids = new ArrayList<>();

        agents.add(new Agent("Mohsen Karimi","mohsen","123456", Arrays.asList("192.168.1.188")));
    }

    public static List<DID> getDIDs() {
        return dids;
    }

    public static List<Domain> getDomains() {
        return domains;
    }

    public static List<Gateway> getGateways() {
        return gateways;
    }

    public static List<Peer> getPeers() {
        return peers;
    }

    public static List<Agent> getAgents() {
        return agents;
    }
}
