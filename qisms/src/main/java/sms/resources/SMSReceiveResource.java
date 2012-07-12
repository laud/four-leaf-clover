package sms.resources;

import com.yammer.metrics.annotation.Timed;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import sms.core.Saying;
import sms.core.Sender;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/11/12
 * Time: 7:12 PM
 */
@Path("/receive")
@Produces(MediaType.APPLICATION_JSON)
public class SMSReceiveResource {

    private final AtomicLong counter = new AtomicLong();
    private final String template;
    private final Logger logger = Logger.getLogger(SMSReceiveResource.class);
    private final String endpoint;

    public SMSReceiveResource(String template, String endpoint) {
        this.template = template;
        this.endpoint = endpoint;
    }
    @GET
    @Timed
    public Saying receiveHello(@QueryParam("Body") String body,
                               @QueryParam("FromCity") String city,
                               @QueryParam("From") String fromNumber,
                               @QueryParam("FromState") String state){
        if (body== null || body.length() == 0) {
            return null;
        }

        String location = "";

        if (city!=null && city.length()>0) {
            location += city;
        }

        if (location.length() > 0) {
            location += ", ";
        }
        if (state != null && state.length() > 0) {
            location += state;
        }


        if (fromNumber.length() > 10) {
            fromNumber =  fromNumber.substring(2);
        }

        String questionId = Sender.questionToPhoneNumber.get(fromNumber);
        if (questionId == null) {
            questionId = "ipod-1342074998416";
        }
        logger.info("From: " + fromNumber);
        logger.info("Answer: " + body);
        logger.info("Location: " + location);
        logger.info("questionId: " + questionId);
        String url = String.format("%s/answer/for/%s?text=%s&number=%s&location=%s",endpoint, URLEncoder.encode(questionId), URLEncoder.encode(body), fromNumber, URLEncoder.encode(location));
        logger.info("postURL: "+url);
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        Integer statusCode = null;
        try {
            statusCode = httpClient.executeMethod(getMethod);
        } catch (HttpException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (statusCode == HttpStatus.SC_OK) {
            logger.info("Post to create answer successful!");
        } else {
            logger.info("Post to create answer unsuccessful: " + statusCode.toString());
        }

        return new Saying(counter.incrementAndGet(),
                                  String.format(template, city, body, "true"));
    }
}
