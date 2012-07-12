package com.bazaarvoice.emodb.examples.skeleton;

import com.bazaarvoice.emodb.databus.api.Databus;
import com.bazaarvoice.emodb.databus.client.DatabusClientFactory;
import com.bazaarvoice.emodb.esquire.api.Esquire;
import com.bazaarvoice.emodb.examples.skeleton.config.DatabusListeningConfiguration;
import com.bazaarvoice.emodb.examples.skeleton.config.SkeletonConfiguration;
import com.bazaarvoice.emodb.examples.skeleton.databus.DatabusResource;
import com.bazaarvoice.emodb.sor.api.DataStore;
import com.bazaarvoice.emodb.sor.client.DataStoreClientFactory;
import com.bazaarvoice.emodb.sor.client.DataStoreFixedHostDiscoverySource;
import com.bazaarvoice.soa.discovery.ZooKeeperHostDiscovery;
import com.bazaarvoice.soa.pool.ServicePoolBuilder;
import com.bazaarvoice.soa.retry.RetryNTimes;
import com.bazaarvoice.soa.zookeeper.ZooKeeperConfiguration;
import com.bazaarvoice.soa.zookeeper.ZooKeeperConnection;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.yammer.dropwizard.client.JerseyClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SkeletonModule extends AbstractModule {

    private final SkeletonConfiguration skeletonConfiguration;
    private ZooKeeperConnection zooKeeperConnection;
    private ExecutorService databusPollingExecutorService;
    private ScheduledExecutorService databusSubscriptionExecutorService;
    private Provider<JerseyClient> jerseyClientProvider;

    public SkeletonModule(SkeletonConfiguration skeletonConfiguration) {
        this.skeletonConfiguration = skeletonConfiguration;
    }

    public void setZooKeeperConnection(ZooKeeperConnection zooKeeperConnection) {
        this.zooKeeperConnection = zooKeeperConnection;
    }

    public void setDatabusPollingExecutorService(ExecutorService databusPollingExecutorService) {
        this.databusPollingExecutorService = databusPollingExecutorService;
    }

    public void setDatabusSubscriptionExecutorService(ScheduledExecutorService databusSubscriptionExecutorService) {
        this.databusSubscriptionExecutorService = databusSubscriptionExecutorService;
    }

    public void setJerseyClientProvider(Provider<JerseyClient> jerseyClientProvider) {
        this.jerseyClientProvider = jerseyClientProvider;
    }



    protected void configure() {
        bind(DataStore.class).toInstance(createDataStoreClient());  // The client you will use to access the System of Record
        bind(Databus.class).toInstance(createDataBusClient());      // The client you will use to access the Databus
        bind(Esquire.class).toInstance(createEsquireClient());      // The client (or example thereof) you will use to access ElasticSearch

        bind(DatabusListeningConfiguration.class).toInstance(skeletonConfiguration.getDatabusListeningConfiguration());
        bind(ExecutorService.class).annotatedWith(DatabusResource.class).toInstance(databusPollingExecutorService);
        bind(ScheduledExecutorService.class).annotatedWith(DatabusResource.class).toInstance(databusSubscriptionExecutorService);
    }

    private DataStore createDataStoreClient() {
        ThreadFactory daemonThreadFactory = new ThreadFactoryBuilder().setDaemon(true).build();
        DataStoreClientFactory dataStoreClientFactory = new DataStoreClientFactory(jerseyClientProvider.get());  // Creates an internal Apache HTTP Client
        return ServicePoolBuilder.create(DataStore.class)
                .withHostDiscoverySource(skeletonConfiguration.getSorEndPointOverrides())
                .withHostDiscovery(new ZooKeeperHostDiscovery(zooKeeperConnection, dataStoreClientFactory.getServiceName()))
                .withServiceFactory(dataStoreClientFactory)
                .withHealthCheckExecutor(Executors.newScheduledThreadPool(1, daemonThreadFactory))
                .buildProxy(new RetryNTimes(3, 100, TimeUnit.MILLISECONDS));
    }

    private Databus createDataBusClient() {
        ThreadFactory daemonThreadFactory = new ThreadFactoryBuilder().setDaemon(true).build();
        DatabusClientFactory databusClientFactory = new DatabusClientFactory(jerseyClientProvider.get());  // Creates an internal Apache HTTP Client
        return ServicePoolBuilder.create(Databus.class)
                .withHostDiscoverySource(skeletonConfiguration.getDatabusEndPointOverrides())
                .withHostDiscovery(new ZooKeeperHostDiscovery(zooKeeperConnection, databusClientFactory.getServiceName()))
                .withServiceFactory(databusClientFactory)
                .withHealthCheckExecutor(Executors.newScheduledThreadPool(1, daemonThreadFactory))
                .buildProxy(new RetryNTimes(3, 100, TimeUnit.MILLISECONDS));
    }

    private Esquire createEsquireClient() {
        return new Esquire(createElasticSearchConnection());
    }

    private Client createElasticSearchConnection() {
        return new TransportClient(elasticSearchClientSettings())
                .addTransportAddress(new InetSocketTransportAddress(skeletonConfiguration.getElasticSearchConfiguration().getHostName(), skeletonConfiguration.getElasticSearchConfiguration().getPort()));
    }

    private ImmutableSettings.Builder elasticSearchClientSettings() {
        return ImmutableSettings.settingsBuilder()
                .put("cluster.name", skeletonConfiguration.getElasticSearchConfiguration().getClusterName());
    }
}
