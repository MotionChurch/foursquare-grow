package com.p4square.grow.backend;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.p4square.grow.config.Config;
import org.apache.log4j.Logger;

/**
 * Send Notifications via SimpleEmailService.
 */
public class SESNotificationService implements NotificationService {

    private final static Logger LOG = Logger.getLogger(SESNotificationService.class);

    private final AmazonSimpleEmailService mClient;
    private final String mSourceAddress;
    private final Destination mDestination;

    public SESNotificationService(final Config config) {
        AWSCredentials creds;

        String awsAccessKey = config.getString("awsAccessKey");
        if (awsAccessKey != null) {
            creds = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return config.getString("awsAccessKey");
                }
                @Override
                public String getAWSSecretKey() {
                    return config.getString("awsSecretKey");
                }
            };
        } else {
            creds = new DefaultAWSCredentialsProviderChain().getCredentials();
        }

        mClient = new AmazonSimpleEmailServiceClient(creds);

        String region = config.getString("awsRegion");
        if (region != null) {
            mClient.setRegion(Region.getRegion(Regions.fromName(region)));
        }

        mSourceAddress = config.getString("notificationSourceEmail");

        final String dest = config.getString("notificationEmail");
        if (dest != null) {
            mDestination = new Destination().withToAddresses(dest);
        } else {
            // Notifications are not configured.
            mDestination = null;
        }
    }

    @Override
    public void sendNotification(final String message) {
        try {
            if (mSourceAddress == null || mDestination == null) {
                // Disable notifications if there is no source address configured.
                LOG.debug("Notifications are disabled because source or destination emails are not configured.");
                return;
            }

            Message msg = new Message()
                    .withSubject(new Content().withCharset("UTF-8").withData("Grow Notification"))
                    .withBody(new Body()
                            .withText(new Content().withCharset("UTF-8").withData(message)));

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(mDestination)
                    .withSource(mSourceAddress)
                    .withMessage(msg);

            mClient.sendEmail(request);

        } catch (Exception e) {
            LOG.warn("Failed to send notification email", e);
        }
    }
}
