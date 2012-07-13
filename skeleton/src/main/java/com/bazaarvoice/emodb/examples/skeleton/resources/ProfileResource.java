package com.bazaarvoice.emodb.examples.skeleton.resources;

import com.bazaarvoice.emodb.esquire.api.Entity;
import com.bazaarvoice.emodb.esquire.api.Esquire;
import com.bazaarvoice.emodb.examples.skeleton.WellKnowns;
import com.bazaarvoice.emodb.sor.api.*;
import com.bazaarvoice.emodb.sor.delta.Deltas;
import com.bazaarvoice.emodb.sor.uuid.TimeUUIDs;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.yammer.dropwizard.jersey.params.IntParam;
import org.elasticsearch.common.collect.Maps;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/12/12
 * Time: 12:41 PM
 */
@Path("/profile")
public class ProfileResource {
    public static final String TABLE = "clover:profile";

    @Inject private DataStore sorClient;
    @Inject private Esquire esClient;

    private boolean tableExists;


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

    @GET
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProfile (@QueryParam("phone_num")  String phoneNum, @QueryParam("name") final String name) {
        createTableIfNonExistant();
        if (phoneNum==null || name ==null) {
            return Response.noContent().build();
        }
        final String newId = ""+(new Date()).getTime();
        final String cleanPhoneNumber = phoneNum.replaceAll("\\D","");
        sorClient.update(
                TABLE,
                newId,
                TimeUUIDs.newUUID(),
                Deltas.mapBuilder().putAll(new HashMap<String, Object>() {{ put("phone_num", cleanPhoneNumber); }}).put("name", name).put("type", "profile").build(),
                new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Creating profile").build(),
                WriteConsistency.STRONG);
//        return Response.ok().build();

        Map<String, String> map = new HashMap<String, String>() {{ put("ok", newId); }};
        return Response.ok(
                map,
                MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("all")
    @Produces (MediaType.APPLICATION_JSON)
    public Collection<Map<String, Object>> getAllProfiles (@QueryParam("limit") @DefaultValue("100") IntParam limit) {
        createTableIfNonExistant();
        List<Entity> entities = esClient.queryTable(TABLE).type("profile").limit(limit.get()).execute();
        return Collections2.transform(entities, new Function<Entity, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(@Nullable Entity input) {
                return input.asMap();
            }
        });

    }
}
