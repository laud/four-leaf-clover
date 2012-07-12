package sms.core;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.AvailablePhoneNumber;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: qi
 * Date: 7/11/12
 * Time: 4:58 PM
 */
public class Sender {
    private final String number;
    private final String text;
    public static final String ACCOUNT_SID = "AC8a5090866f4651cec38aecf9fafb6587";
    public static final String AUTH_TOKEN = "13b3090e2b867893b64ce4ac18acc7f9";
    public  static final Map<String, String> questionToPhoneNumber = new HashMap<String, String>();
    public Sender (String number, String text){
        this.number = number;
        this.text = text;
    }

    public String getNumber() {
        return number;
    }
    public String getText() {
        return text;
    }

    public boolean sendText() {
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
        Account mainAccount = client.getAccount();
        mainAccount.getSmsMessages();

        SmsFactory smsFactory = mainAccount.getSmsFactory();
        Map<String, String> smsParams = new HashMap<String, String>();
        smsParams.put("To", number); // Replace with a valid phone number
        smsParams.put("From", "(415) 702-3337"); // Replace with a valid phone number in your account
        smsParams.put("Body", text);


        try {
            smsFactory.create(smsParams);
        } catch (TwilioRestException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }


}
