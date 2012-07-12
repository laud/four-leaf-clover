package com.bazaarvoice.emodb.examples.skeleton.config;

import com.yammer.dropwizard.config.Configuration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/12/12
 * Time: 11:04 AM
 */
public class CloverConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String phoneNumbers;

    public String getPhoneNumbers(){
        return phoneNumbers;
    }


}
