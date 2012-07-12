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
 * Created with IntelliJ IDEA.
 * User: daxiage
 * Date: 7/11/12
 * Time: 11:31 PM
 */

@Path("/answer")
public class AnswerResource {
    private static final String TABLE = "clover:answer";

    @Inject private DataStore sorClient;
    @Inject private Esquire esClient;

    private boolean tableExists;

    @GET
    @Path("for/{question_id}")
    public Response createAnswer (@PathParam("question_id") String question_id, @QueryParam("text") final String text,
                                  @QueryParam("location") final String location, @QueryParam("phone") final String phone) {
        createTableIfNonExistant();
        final String newId = ""+(new Date()).getTime();
        sorClient.update(
                TABLE,
                newId,
                TimeUUIDs.newUUID(),
                Deltas.mapBuilder().put("question_id",question_id).put("text",text).put("location",location).put("phone",phone).put("type", "answer").build(),
                new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Creating answer").build(),
                WriteConsistency.STRONG);
//        return Response.ok().build();

        Map<String, String> map = new HashMap<String, String>() {{ put("ok", newId); }};
        return Response.ok(
                map,
                MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("{answerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnswer (@PathParam("answerId") String answerId) {
        createTableIfNonExistant();
        return Response.ok(
                sorClient.get(TABLE, answerId, ReadConsistency.WEAK),
                MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("list/{question_id}")
    @Produces (MediaType.APPLICATION_JSON)
    public Collection<Map<String, Object>> listAllAnswersForAQuestion (@PathParam("question_id") String question_id, @QueryParam("limit") @DefaultValue("100") IntParam limit) {
        createTableIfNonExistant();
        List<Entity> entities = esClient.queryTable(TABLE).type("answer").with("question_id", question_id).limit(limit.get()).execute();
        return Collections2.transform(entities, new Function<Entity, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(@Nullable Entity input) {
                return input.asMap();
            }
        });
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
