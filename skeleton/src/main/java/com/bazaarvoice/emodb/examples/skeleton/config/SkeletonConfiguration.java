package com.bazaarvoice.emodb.examples.skeleton.config;

import com.bazaarvoice.emodb.databus.client.DatabusFixedHostDiscoverySource;
import com.bazaarvoice.emodb.sor.client.DataStoreFixedHostDiscoverySource;
import com.bazaarvoice.soa.zookeeper.ZooKeeperConfiguration;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SkeletonConfiguration extends Configuration {
    @JsonProperty
    private ZooKeeperConfiguration zooKeeper;

    @JsonProperty
    private ElasticSearchConfiguration elasticSearch;

    /** Configured static list of System of Record servers overrides ZooKeeper, if specified. */
    @JsonProperty
    private DataStoreFixedHostDiscoverySource sorEndPointOverrides = new DataStoreFixedHostDiscoverySource();

    /** Configured static list of Databus servers overrides ZooKeeper, if specified. */
    @JsonProperty
    private DatabusFixedHostDiscoverySource dbusEndPointOverrides = new DatabusFixedHostDiscoverySource();

    @JsonProperty
    private DatabusListeningConfiguration databus = new DatabusListeningConfiguration();

    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    public ZooKeeperConfiguration getZooKeeperConfiguration() {
        return zooKeeper;
    }

    public ElasticSearchConfiguration getElasticSearchConfiguration() {
        return elasticSearch;
    }

    public DataStoreFixedHostDiscoverySource getSorEndPointOverrides() {
        return sorEndPointOverrides;
    }

    public DatabusFixedHostDiscoverySource getDatabusEndPointOverrides() {
        return dbusEndPointOverrides;
    }

    public DatabusListeningConfiguration getDatabusListeningConfiguration() {
        return databus;
    }

    public JerseyClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

}
