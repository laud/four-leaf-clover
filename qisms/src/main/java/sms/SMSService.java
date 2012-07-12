package sms;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Environment;
import sms.health.TemplateHealthCheck;
import sms.resources.SMSReceiveResource;
import sms.resources.SMSSendResource;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/11/12
 * Time: 3:44 PM
 */
public class SMSService extends Service<SMSConfiguration> {
    public static void main(String[] args) throws Exception {
        new SMSService().run(args);
    }

    private SMSService() {
        super("sms");
    }
    @Override
    protected void initialize(SMSConfiguration smsConfiguration, Environment environment) throws Exception {
        final String template = smsConfiguration.getTemplate();
        final String defaultName = smsConfiguration.getDefaultName();
        final String endpoint = smsConfiguration.getEndPoint();
        environment.addResource(new SMSSendResource(template, defaultName));
        environment.addResource(new SMSReceiveResource(template, endpoint));
        environment.addHealthCheck(new TemplateHealthCheck(template));
    }
}
