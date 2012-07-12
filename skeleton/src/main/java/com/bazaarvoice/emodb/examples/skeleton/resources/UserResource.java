package com.bazaarvoice.emodb.examples.skeleton.resources;

import com.bazaarvoice.emodb.esquire.api.Entity;
import com.bazaarvoice.emodb.esquire.api.Esquire;
import com.bazaarvoice.emodb.examples.skeleton.WellKnowns;
import com.bazaarvoice.emodb.sor.api.AuditBuilder;
import com.bazaarvoice.emodb.sor.api.DataStore;
import com.bazaarvoice.emodb.sor.api.ReadConsistency;
import com.bazaarvoice.emodb.sor.api.TableOptionsBuilder;
import com.bazaarvoice.emodb.sor.api.WriteConsistency;
import com.bazaarvoice.emodb.sor.delta.Deltas;
import com.bazaarvoice.emodb.sor.delta.MapDeltaBuilder;
import com.bazaarvoice.emodb.sor.uuid.TimeUUIDs;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.yammer.dropwizard.jersey.params.IntParam;
import org.elasticsearch.common.collect.Maps;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Path("/user")
public class UserResource {

    private static final String TABLE = "user:skeletoncustomer";

    @Inject private DataStore sorClient;
    @Inject private Esquire esClient;

    private boolean tableExists;


    @PUT
    @Path("sor/{userid}")
    @Consumes (MediaType.APPLICATION_JSON)
    public Response createUser(@PathParam("userid") String userID, Map<String, Object> userAttributes) {
        createTableIfNonExistant();
        sorClient.update(
                TABLE,
                userID,
                TimeUUIDs.newUUID(),
                Deltas.mapBuilder().putAll(userAttributes).put("type", "user").build(),
                new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Creating user").build(),
                WriteConsistency.STRONG);
        return Response.ok().build();
    }

    @GET
    @Path("sor/{userid}")
    @Produces (MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userid") String userID) {
        createTableIfNonExistant();
        return Response.ok(
                    sorClient.get(TABLE, userID, ReadConsistency.WEAK),
                    MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("es/all")
    @Produces (MediaType.APPLICATION_JSON)
    public Collection<Map<String, Object>> listAllUsers(@QueryParam("limit") @DefaultValue("100") IntParam limit) {
        createTableIfNonExistant();
        List<Entity> entities = esClient.queryTable(TABLE).type("user").limit(limit.get()).execute();
        return Collections2.transform(entities, new Function<Entity, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(@Nullable Entity input) {
                return input.asMap();
            }
        });
    }

    @DELETE
    @Path("sor/{userid}")
    public Response deleteUser(@PathParam("userid") String userID) {
        createTableIfNonExistant();
        //TODO: Implement me as part of the hackathon tutorial. 
        return Response.ok().build();
    }

    @POST
    @Path("sor/{userid}")
    @Consumes (MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("userid") String userID, Map<String, Object> newUserAttributes) {
        createTableIfNonExistant();

        MapDeltaBuilder deltaBuilder = Deltas.mapBuilder();

        Map<String, Object> oldUserAttributes = sorClient.get(TABLE, userID, ReadConsistency.WEAK);
        for(Map.Entry<String, Object> oldEntry : oldUserAttributes.entrySet()) {
            if (newUserAttributes.containsKey(oldEntry.getKey())) {
                if (newUserAttributes.get(oldEntry.getKey()).equals(oldEntry.getValue())) {
                    newUserAttributes.remove(oldEntry.getKey());  // same key and value in old and new map, no sense in updating it
                }
            } else {
                deltaBuilder.remove(oldEntry.getKey());
            }
        }
        deltaBuilder.putAll(newUserAttributes);
        deltaBuilder.putIfAbsent("type", "user");

        sorClient.update(
                TABLE,
                userID,
                TimeUUIDs.newUUID(),
                deltaBuilder.build(),
                new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Updating user").build(),
                WriteConsistency.STRONG);
        return Response.ok().build();
    }


    private void createTableIfNonExistant() {
        if (!tableExists && !sorClient.getTableExists(TABLE)) {
            sorClient.createTable(
                    TABLE,
                    new TableOptionsBuilder().setPlacement("emo:ugc").build(),
                    Maps.<String, Object>newHashMap(),
                    new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Creating table").build());

            tableExists = true;
        }
    }
}
