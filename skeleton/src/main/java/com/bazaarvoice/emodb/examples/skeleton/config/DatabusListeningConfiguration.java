package com.bazaarvoice.emodb.examples.skeleton.config;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public final class DatabusListeningConfiguration {

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in seconds, to wait before performing the initial databus subscription after JVM startup.
     */
    private long initialSubscriptionDelaySeconds = 0L;


    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in seconds, to wait between renewing the databus subscription.
     */
    private long subscriptionIntervalSeconds = 900L;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in seconds, for a subscription to remain alive
     */
    private int subscriptionTimeToLiveSeconds = 86400;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in seconds, before an event stored with the subscription is expired (and no longer eligible to be returned by a poll request).
     */
    private int subscriptionEventTimeToLiveSeconds = 172800;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in seconds, before an event received from the bus is required to be acknowledged before it is redelivered
     */
    private int eventTimeToLiveSeconds = 300;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Subscription name.  Should be unique globally across all listeners on the Databus, so as best practice, prefix with our own app name.
     */
    private String subscriptionName = "skeleton-app";

    @Valid
    @JsonProperty
    @NotNull
    /**
     * The number of simultaneous threads that should be polling the databus.
     */
    private int numberPollingThreads = 5;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * Time, in millis, to wait between subsequent polls of the databus by a single thread.
     */
    private long pollDelayMillis = 2500L;

    @Valid
    @JsonProperty
    @NotNull
    /**
     * The maximum (limit) number of events for a single polling thread to pull off of the databus at any one time.
     */
    private int maxNumEventsPerDatabusPoll = 150;

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

    public long getInitialSubscriptionDelaySeconds() {
        return initialSubscriptionDelaySeconds;
    }
    public long getSubscriptionIntervalSeconds() {
        return subscriptionIntervalSeconds;
    }
    public int getSubscriptionTimeToLiveSeconds() {
        return subscriptionTimeToLiveSeconds;
    }
    public int getSubscriptionEventTimeToLiveSeconds() {
        return subscriptionEventTimeToLiveSeconds;
    }
    public int getEventTimeToLiveSeconds() {
        return eventTimeToLiveSeconds;
    }
    public String getSubscriptionName() {
        return subscriptionName;
    }
    public int getNumberPollingThreads() {
        return numberPollingThreads;
    }
    public long getPollDelayMillis() {
        return pollDelayMillis;
    }
    public int getMaxNumEventsPerDatabusPoll() {
        return maxNumEventsPerDatabusPoll;
    }

}