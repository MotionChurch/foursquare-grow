package com.p4square.grow.backend;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.p4square.grow.config.Config;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link SESNotificationService}.
 */
public class SESNotificationServiceTest {

    private SESNotificationService service;

    private AmazonSimpleEmailService mockSES;
    private SendEmailResult emailResult;

    @Before
    public void setup() {
        mockSES = EasyMock.createMock(AmazonSimpleEmailService.class);
        emailResult = new SendEmailResult().withMessageId("1234");
    }

    @Test
    public void notificationsDisabled() {
        // Setup
        Config testConfig = new Config();
        service = new SESNotificationService(testConfig, mockSES);

        EasyMock.replay(mockSES);

        // Run test
        service.sendNotification("Hello World");

        // Verify
        EasyMock.verify(mockSES);

    }

    @Test
    public void sendNotification() throws Exception {
        // Setup
        Config testConfig = new Config();
        testConfig.setString("notificationSourceEmail", "from@example.com");
        testConfig.setString("notificationEmail", "to@example.com");
        service = new SESNotificationService(testConfig, mockSES);

        Capture<SendEmailRequest> requestCapture = EasyMock.newCapture();
        EasyMock.expect(mockSES.sendEmail(EasyMock.capture(requestCapture))).andReturn(emailResult).once();
        EasyMock.replay(mockSES);

        // Run test
        service.sendNotification("Hello World");

        // Verify
        EasyMock.verify(mockSES);
        SendEmailRequest request = requestCapture.getValue();
        assertEquals("from@example.com", request.getSource());
        assertEquals(1, request.getDestination().getToAddresses().size());
        assertEquals("to@example.com", request.getDestination().getToAddresses().get(0));
    }

    @Test
    public void testMultipleDestinations() {
        // Setup
        Config testConfig = new Config();
        testConfig.setString("notificationSourceEmail", "from@example.com");
        testConfig.setString("notificationEmail", "to@example.com,another@example.com");
        service = new SESNotificationService(testConfig, mockSES);

        Capture<SendEmailRequest> requestCapture = EasyMock.newCapture();
        EasyMock.expect(mockSES.sendEmail(EasyMock.capture(requestCapture))).andReturn(emailResult).once();
        EasyMock.replay(mockSES);


        // Run test
        service.sendNotification("Hello World");

        // Verify
        EasyMock.verify(mockSES);
        SendEmailRequest request = requestCapture.getValue();
        assertEquals("from@example.com", request.getSource());
        assertEquals(2, request.getDestination().getToAddresses().size());
        assertEquals("to@example.com", request.getDestination().getToAddresses().get(0));
        assertEquals("another@example.com", request.getDestination().getToAddresses().get(1));
    }

}