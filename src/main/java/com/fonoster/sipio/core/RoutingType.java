package com.fonoster.sipio.core;

public enum RoutingType {
    DOMAIN_INGRESS_ROUTING, // Calling from the PSTN to an Agent or Peer
    DOMAIN_EGRESS_ROUTING, // Calling from an Agent to the PSTN thru a Gateway
    INTER_DOMAIN_ROUTING, // Caling from and Agent(or Peer) to other Agent (or Peer) that is'nt in same domain
    INTRA_DOMAIN_ROUTING,// Routing type for calling within the same Domain
    PEER_EGRESS_ROUTING, // Similar to DER but applies only to Peers
    UNKNOWN
}
