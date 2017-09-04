package com.p4square.grow.backend;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import com.p4square.grow.config.Config;
import com.p4square.grow.config.ConfigCredentialProvider;
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
        this(config, new AmazonSimpleEmailServiceClient(new ConfigCredentialProvider(config)));

        // Set the AWS region.
        String region = config.getString("awsRegion");
        if (region != null) {
            mClient.setRegion(Region.getRegion(Regions.fromName(region)));
        }
    }

    public SESNotificationService(Config config, AmazonSimpleEmailService client) {
        mClient = client;

        mSourceAddress = config.getString("notificationSourceEmail");

        final String[] dests = config.getString("notificationEmail", "").split(",");
        mDestination = new Destination().withToAddresses(dests);
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
