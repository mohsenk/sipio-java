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

        JsonElement agentsJson = new ResourceUtil().getJson("config/agents.yml");
        for (JsonElement agentJson : agentsJson.getAsJsonArray()) {
            agents.add(new Agent(agentJson.getAsJsonObject()));
        }

        JsonElement peersJson = new ResourceUtil().getJson("config/peers.yml");
        for (JsonElement peerJson : peersJson.getAsJsonArray()) {
            peers.add(new Peer(peerJson.getAsJsonObject()));
        }

        JsonElement domainsJson = new ResourceUtil().getJson("config/domains.yml");
        for (JsonElement domainJson : domainsJson.getAsJsonArray()) {
            domains.add(new Domain(domainJson.getAsJsonObject()));
        }

        JsonElement gatewaysJson = new ResourceUtil().getJson("config/gateways.yml");
        for (JsonElement gatewayJson : gatewaysJson.getAsJsonArray()) {
            gateways.add(new Gateway(gatewayJson.getAsJsonObject()));
        }

        JsonElement didsJson = new ResourceUtil().getJson("config/dids.yml");
        for (JsonElement didJson : didsJson.getAsJsonArray()) {
            dids.add(new DID(didJson.getAsJsonObject()));
        }


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
