package com.bazaarvoice.emodb.examples.skeleton;

import com.bazaarvoice.emodb.examples.skeleton.config.SkeletonConfiguration;
import com.bazaarvoice.emodb.examples.skeleton.databus.ManagedDatabusListener;
import com.bazaarvoice.emodb.examples.skeleton.resources.AnswerResource;
import com.bazaarvoice.emodb.examples.skeleton.resources.ProfileResource;
import com.bazaarvoice.emodb.examples.skeleton.resources.QuestionResource;
import com.bazaarvoice.emodb.examples.skeleton.resources.UserResource;
import com.bazaarvoice.soa.zookeeper.ZooKeeperConnection;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.client.JerseyClientFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterConfiguration;
import com.yammer.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SkeletonService extends Service<SkeletonConfiguration> {

    public static void main(String[] args) throws Exception {
        new SkeletonService().run(args);
    }

    protected SkeletonService() {
        super("skeleton");

        addBundle(new AssetsBundle("/static", "/static/"));
    }

    protected void initialize(SkeletonConfiguration configuration, Environment environment) throws Exception {
        Injector injector = Guice.createInjector(createSkeletonModule(configuration, environment));

        environment.manage(injector.getInstance(ManagedDatabusListener.class));

//        environment.addResource(injector.getInstance(UserResource.class));
        environment.addResource(injector.getInstance(QuestionResource.class));
        environment.addResource(injector.getInstance(AnswerResource.class));
        environment.addResource(injector.getInstance(ProfileResource.class));
    }

    private SkeletonModule createSkeletonModule(SkeletonConfiguration configuration, Environment environment) {
        SkeletonModule skeletonModule = new SkeletonModule(configuration);
        skeletonModule.setZooKeeperConnection(createZooKeeperConnection(configuration, environment));
        skeletonModule.setDatabusPollingExecutorService(createDatabusPollingExecutorService(configuration, environment));
        skeletonModule.setDatabusSubscriptionExecutorService(createDatabusSubscriptionExecutorService(environment));
        skeletonModule.setJerseyClientProvider(createJerseyClientProvider(configuration, environment));
        return skeletonModule;
    }

    private ZooKeeperConnection createZooKeeperConnection(SkeletonConfiguration configuration, Environment environment) {
        final ZooKeeperConnection cxn = configuration.getZooKeeperConfiguration().connect();
        environment.manage(new Managed() {
            @Override
            public void start() throws Exception {
                // no-op
            }
            @Override
            public void stop() throws Exception {
                cxn.close();
            }
        });
        return cxn;
    }

    private ExecutorService createDatabusPollingExecutorService(SkeletonConfiguration configuration, Environment environment) {
        return environment.managedExecutorService("databus-worker-%d",
                configuration.getDatabusListeningConfiguration().getNumberPollingThreads(),
                configuration.getDatabusListeningConfiguration().getNumberPollingThreads(),
                5,
                TimeUnit.SECONDS);
    }

    private ScheduledExecutorService createDatabusSubscriptionExecutorService(Environment environment) {
        return environment.managedScheduledExecutorService("databus-subscriber-%d", 1);
    }

    private Provider<JerseyClient> createJerseyClientProvider(final SkeletonConfiguration configuration, final Environment environment) {
        return new Provider<JerseyClient>() {
            @Override
            public JerseyClient get() {
                return new JerseyClientFactory(configuration.getHttpClientConfiguration()).build(environment);
            }
        };
    }

}
