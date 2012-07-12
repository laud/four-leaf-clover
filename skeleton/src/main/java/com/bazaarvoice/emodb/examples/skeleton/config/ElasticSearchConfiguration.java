package com.bazaarvoice.emodb.examples.skeleton.config;

import org.codehaus.jackson.annotate.JsonProperty;

public class ElasticSearchConfiguration {

    @JsonProperty
    private String clusterName;

    @JsonProperty
    private String hostName;

    @JsonProperty
    private int port;

    public String getClusterName() {
        return clusterName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

}
