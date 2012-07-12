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
 * Time: 3:06 PM
 */
@Path("/question")
public class QuestionResource {

    private static final String TABLE = "clover:question";

    @Inject
    private DataStore sorClient;
    @Inject private Esquire esClient;

    private boolean tableExists;

    @GET
    @Path("for/{product_id}")
    @Produces (MediaType.APPLICATION_JSON)
    public Response createQuestion (@PathParam("product_id") String productId, @QueryParam("text") final String text) {
        createTableIfNonExistant();
        if (text==null) {
            return Response.noContent().build();
        }
        final String newId = ""+(new Date()).getTime();
        sorClient.update(
                TABLE,
                newId,
                TimeUUIDs.newUUID(),
                Deltas.mapBuilder().putAll(new HashMap<String, Object>() {{ put("text", text); }}).put("product_id", productId).put("type", "question").build(),
                new AuditBuilder().setLocalHost().setProgram(WellKnowns.APP_NAME).setComment("Creating question").build(),
                WriteConsistency.STRONG);
//        return Response.ok().build();

        Map<String, String> map = new HashMap<String, String>() {{ put("ok", newId); }};
        return Response.ok(
                map,
                MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("{question_id}")
    @Produces (MediaType.APPLICATION_JSON)
    public Response getQuestion (@PathParam("question_id") String questionId) {
        createTableIfNonExistant();
        return Response.ok(
                sorClient.get(TABLE, questionId, ReadConsistency.WEAK),
                MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("list/{product_id}")
    @Produces (MediaType.APPLICATION_JSON)
    public Collection<Map<String, Object>> listAllQuestionsForAProduct (@PathParam("product_id") String productId, @QueryParam("limit") @DefaultValue("100") IntParam limit) {
        createTableIfNonExistant();
        List<Entity> entities = esClient.queryTable(TABLE).type("question").with("product_id", productId).limit(limit.get()).execute();
        return Collections2.transform(entities, new Function<Entity, Map<String, Object>>() {
            @Override
            public Map<String, Object> apply(@Nullable Entity input) {
                return input.asMap();
            }
        });
    }
    //

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
