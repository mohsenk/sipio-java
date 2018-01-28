package com.fonoster.sipio.repository;

import com.fonoster.sipio.core.ConfigManager;
import com.fonoster.sipio.core.model.Agent;

import java.util.List;

public class AgentRepository {

    public static Agent getAgent(String domainUri, String username) {
        for (Agent agent : getAgents()) {
            if (agent.getUsername().equals(username)) {
                for (String domain : agent.getDomains()) {
                    if (domain.equals(domainUri)) {
                        return agent;
                    }
                }
            }
        }
        return null;
    }

    public  static boolean agentExist(String domainUri, String username) {
        return getAgent(domainUri, username) != null;
    }

    public static List<Agent> getAgents() {
        return ConfigManager.getAgents();
    }

}
