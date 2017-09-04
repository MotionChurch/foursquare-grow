package com.p4square.grow.backend.feed;

import com.p4square.grow.backend.GrowBackend;
import com.p4square.grow.backend.NotificationService;
import com.p4square.grow.model.Message;
import com.p4square.grow.model.MessageThread;
import com.p4square.grow.provider.CollectionProvider;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for the feed's TopicResource.
 */
public class TopicResourceTest {

    private TopicResource resource;

    private GrowBackend mockBackend;
    private CollectionProvider<String, String, MessageThread> mockProvider;
    private NotificationService mockNotificationService;

    @Before
    public void setup() {
        mockNotificationService = EasyMock.createMock(NotificationService.class);
        mockProvider = EasyMock.createMock(CollectionProvider.class);

        mockBackend = EasyMock.createMock(GrowBackend.class);
        EasyMock.expect(mockBackend.getThreadProvider()).andReturn(mockProvider).anyTimes();
        EasyMock.expect(mockBackend.getNotificationService()).andReturn(mockNotificationService).anyTimes();

        resource = new TopicResource();
        resource.setApplication(mockBackend);
    }

    @Test
    public void testNotification() throws Exception {
        // Prepare request
        Message message = new Message();
        message.setMessage("Test message");
        MessageThread thread = new MessageThread();
        thread.setMessage(message);
        Representation entity = new JacksonRepresentation<>(thread);

        Request request = new Request(Method.POST, "/feed/leader");
        request.getAttributes().put("topic", "leader");

        // Set expectations
        mockProvider.put(EasyMock.eq("leader"), EasyMock.anyString(), EasyMock.anyObject(MessageThread.class));
        mockNotificationService.sendNotification("A new question was posted on the leader topic:\n\nTest message");
        EasyMock.replay(mockBackend, mockProvider, mockNotificationService);

        // Test
        resource.setRequest(request);
        resource.doInit();
        Representation result = resource.post(entity);

        // Verify
        EasyMock.verify(mockBackend, mockProvider, mockNotificationService);

        assertNotNull(result);
    }

}