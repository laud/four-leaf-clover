package com.bazaarvoice.emodb.examples.skeleton.databus;

import com.bazaarvoice.emodb.databus.api.Databus;
import com.bazaarvoice.emodb.databus.api.Event;
import com.bazaarvoice.emodb.databus.api.EventKey;
import com.bazaarvoice.emodb.esquire.api.Entity;
import com.bazaarvoice.emodb.esquire.api.Esquire;
import com.bazaarvoice.emodb.examples.skeleton.config.DatabusListeningConfiguration;
import com.bazaarvoice.emodb.examples.skeleton.resources.ProfileResource;
import com.bazaarvoice.emodb.sor.api.DataStore;
import com.bazaarvoice.emodb.sor.condition.Conditions;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import com.google.inject.Inject;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.collections.CollectionUtils;

import org.apache.http.entity.StringEntity;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.*;
import java.io.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManagedDatabusListener implements Managed {

    private final Log log = Log.forClass(getClass());

    @Inject private Esquire esClient;
    @Inject private Databus databusClient;
    @Inject @DatabusResource private ExecutorService databusPollingExecutorService;
    @Inject @DatabusResource private ScheduledExecutorService databusSubscriptionExecutorService;
    @Inject private DatabusListeningConfiguration databusListeningConfiguration;

    @Override
    public void start() throws Exception {

        //Perform the initial subscription to listen to EVERYTHING
        databusClient.subscribe(databusListeningConfiguration.getSubscriptionName(), Conditions.alwaysTrue(), 86400, 86400);

        // Start a number of threads equal to "core" threads that will poll the databus independently looking for work.
        for (int threadCtr = 0; threadCtr < databusListeningConfiguration.getNumberPollingThreads(); ++threadCtr) {
            databusPollingExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            //log.info("[{}] Polling databus", Thread.currentThread().getName());
                            final List<Event> events = databusClient.poll(
                                    databusListeningConfiguration.getSubscriptionName(),
                                    databusListeningConfiguration.getEventTimeToLiveSeconds(),
                                    databusListeningConfiguration.getMaxNumEventsPerDatabusPoll());

                            if (CollectionUtils.isNotEmpty(events)) {
                                // Do something useful with the event
                                for (Event event : events) {
                                    if("question".equals(event.getContent().get("type"))){
                                        handleQuestionBySMS(event);
                                        handleQuestionByiPhone(event);
                                    }

                                    log.info("[{}] Received event: key={}, contents={}", Thread.currentThread().getName(), event.getEventKey(), event.getContent());
                                }

                                // Now that we've done "something" with the events, we need to ack them to prevent them from being repeatedly redelivered.
                                databusClient.acknowledge(databusListeningConfiguration.getSubscriptionName(), Collections2.transform(events, new Function<Event, EventKey>() {
                                    @Override
                                    public EventKey apply(@Nullable Event input) {
                                        return input.getEventKey();
                                    }
                                }));
                            }
                        } catch (Throwable t) {
                            // These polling threads should be as resilient as possible.
                            log.error(t, "[{}] An uncaught exception occurred while polling for events from the Databus.  Stacktrace follows: ", Thread.currentThread().getName());
                        }

                        // Slow things down just a little bit to avoid breaking a sweat
                        try {
                            if (!Thread.interrupted()) {
                                Thread.sleep(databusListeningConfiguration.getPollDelayMillis());
                            }
                        } catch (InterruptedException e) { /* no-op */ }

                        // Allow the thread to die if the interrupted flag is set (will get set by the Thread Pool when it wants to shut down).
                        if (Thread.interrupted()) {
                            break;
                        }
                    }
                    log.info("DatabusPollerThread[name={}] exiting",  Thread.currentThread().getName());
                }
            });
            try {
                Thread.sleep(1000L); // Delay starting the threads a little bit so that they don't all slam the Databus at the same time.
            } catch (InterruptedException e) {/* no-op */}
        }


        // Create a scheduled and repeating action that will subscribe to the databus as long as this service remains alive.
        databusSubscriptionExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Renew subscription to the databus
                            log.info("Renewing subscription to the databus");
                            databusClient.subscribe(
                                    databusListeningConfiguration.getSubscriptionName(),
                                    Conditions.alwaysTrue(),
                                    databusListeningConfiguration.getSubscriptionTimeToLiveSeconds(),
                                    databusListeningConfiguration.getSubscriptionEventTimeToLiveSeconds());
                        } catch (Throwable t) {
                            // Any exception or abnormal termination of this thread within the single-threaded ScheduledExecutorService will cause subsequent scheduled executions to be skipped.
                            log.error(t, "Error while renewing subscription to the databus");
                        }
                    }
                },
                databusListeningConfiguration.getInitialSubscriptionDelaySeconds(),
                databusListeningConfiguration.getSubscriptionIntervalSeconds(),
                TimeUnit.SECONDS);
    }


    private void handleQuestionBySMS(Event event) throws Exception{
        String id = (String) event.getContent().get("_id");
        String text = (String) event.getContent().get("text");
        if (text==null){
            return;
        }
        List<String> subscribers = getSubscribersForThisProduct (event);
        for (String phoneNumber : subscribers) {
            URL smsService = new URL("http://ec2-23-22-57-59.compute-1.amazonaws.com:8080/send?questionId="+id+"&number="+phoneNumber+"&text=" + URLEncoder.encode(text, "UTF-8"));
            URLConnection yc = smsService.openConnection();
            BufferedReader in = new BufferedReader( new InputStreamReader(yc.getInputStream()) );
            in.close();
        }
    }

    private List<String> getSubscribersForThisProduct(Event event) {
        //todo: filter by product
        List<Entity> entities = esClient.queryTable(ProfileResource.TABLE).type("profile").limit(20).execute();
        Set<String> phoneNums = new HashSet<String>();
        System.out.println("Found " + entities.size()+" subscribers");
        for (Entity e: entities) {
            phoneNums.add( (String) e.get("phone_num") );
        }
        return new ArrayList<String>(phoneNums);
    }

    private static final String CHARSET = "UTF-8";
    static private class JsonEntity extends StringEntity
    {

        public JsonEntity(String jsonString) throws UnsupportedEncodingException
        {
            super(jsonString, CHARSET);
        }


        @Override
        public Header getContentType()
        {
            Header h = new BasicHeader("Content-Type", "application/json");
            return h;
        }

    }

    private void handleQuestionByiPhone(Event event) throws Exception {

        String url = "https://go.urbanairship.com/api/push/broadcast/";
        HttpPost post = new HttpPost(url);

        String j = "{\"aps\": { \"alert\": \"A new question has been asked!\"} }";
        JsonEntity je = new JsonEntity(j);
        post.setEntity( je );

        try
        {
            post.setHeader(new BasicHeader("Accept", "application/json"));
            HttpResponse rsp = createHttpClient().execute(post);
//            checkResponse(method, rsp);
//            return rsp;
        }
        catch (RuntimeException rtex)
        {
            throw rtex;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    protected HttpClient createHttpClient()
    {
        DefaultHttpClient client = new DefaultHttpClient();

        client.getParams().setParameter(AllClientPNames.USER_AGENT, "urbanairship-java library");

        CredentialsProvider credProvider = new BasicCredentialsProvider();

        credProvider.setCredentials(
                new AuthScope("go.urbanairship.com", AuthScope.ANY_PORT),
                new UsernamePasswordCredentials("ePWyTTiVTZyLNt_6Z5NmUQ", "84xyFUIaRM6lK18SsTizeg"));

        client.setCredentialsProvider(credProvider);

        return client;
    }

    @Override
    public void stop() throws Exception {
    }
}
