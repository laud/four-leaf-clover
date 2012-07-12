package sms.resources;

import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import sms.core.Saying;
import sms.core.Sender;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/11/12
 * Time: 3:56 PM
 */
@Path("/send")
@Produces(MediaType.APPLICATION_JSON)
public class SMSSendResource {

    private final String template;
    private final String defaultName;
    private final AtomicLong counter;


    public SMSSendResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Saying sayHello(@QueryParam("number") String number,
                           @QueryParam("text") String text,
                           @QueryParam("questionId") String questionId) {

        Sender sender = new Sender(number, text);
        boolean result = sender.sendText();
        if (result) {
            Sender.questionToPhoneNumber.put(number, questionId);
        }
        return new Saying(counter.incrementAndGet(),
                          String.format(template, number, text, result));
    }
}
