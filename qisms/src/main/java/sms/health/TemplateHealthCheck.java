package sms.health;

import com.yammer.metrics.core.HealthCheck;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/11/12
 * Time: 4:11 PM
 */
public class TemplateHealthCheck extends HealthCheck {
    private final String template;

    public TemplateHealthCheck(String template) {
        super("template");
        this.template = template;
    }

    @Override
    protected Result check() throws Exception {
        final String saying = String.format(template, "TEST");
        if (!saying.contains("TEST")) {
            return Result.unhealthy("template doesn't include a name");
        }
        return Result.healthy();
    }
}
