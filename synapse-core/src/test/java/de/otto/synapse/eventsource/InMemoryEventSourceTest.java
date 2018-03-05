package de.otto.synapse.eventsource;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.synapse.channel.ChannelPosition;
import de.otto.synapse.channel.InMemoryChannel;
import de.otto.synapse.consumer.EventSourceNotification;
import de.otto.synapse.consumer.MessageConsumer;
import de.otto.synapse.message.Message;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static de.otto.synapse.message.Message.message;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class InMemoryEventSourceTest {

    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldSendEventInStreamToConsumer() {
        // given
        InMemoryChannel inMemoryChannel = new InMemoryChannel();
        InMemoryEventSource inMemoryEventSource = new InMemoryEventSource("es","some-stream", inMemoryChannel, eventPublisher, objectMapper);
        StringMessageConsumer eventConsumer = new StringMessageConsumer();
        inMemoryEventSource.register(eventConsumer);
        inMemoryChannel.send(message("key", "payload"));

        // when
        inMemoryEventSource.consumeAll(event -> true);


        // then
        assertThat(eventConsumer.message.getKey(), is("key"));
        assertThat(eventConsumer.message.getPayload(), is("payload"));
    }

    @Test
    public void shouldPublishStartedAndFinishedEvents() {
        // given
        InMemoryChannel inMemoryChannel = new InMemoryChannel();
        InMemoryEventSource inMemoryEventSource = new InMemoryEventSource("es", "some-stream", inMemoryChannel, eventPublisher, objectMapper);
        StringMessageConsumer eventConsumer = new StringMessageConsumer();
        inMemoryEventSource.register(eventConsumer);
        inMemoryChannel.send(message("key", "payload"));

        // when
        inMemoryEventSource.consumeAll(event -> true);


        // then
        ArgumentCaptor<EventSourceNotification> notificationArgumentCaptor = ArgumentCaptor.forClass(EventSourceNotification.class);
        verify(eventPublisher, times(2)).publishEvent(notificationArgumentCaptor.capture());

        EventSourceNotification startedEvent = notificationArgumentCaptor.getAllValues().get(0);
        assertThat(startedEvent.getStatus(), is(EventSourceNotification.Status.STARTED));
        assertThat(startedEvent.getChannelPosition(), is(ChannelPosition.fromHorizon()));
        assertThat(startedEvent.getStreamName(), is("some-stream"));

        EventSourceNotification finishedEvent = notificationArgumentCaptor.getAllValues().get(1);
        assertThat(finishedEvent.getStatus(), is(EventSourceNotification.Status.FINISHED));
        assertThat(finishedEvent.getChannelPosition(), is(nullValue()));
        assertThat(finishedEvent.getStreamName(), is("some-stream"));
    }
    
    private static class StringMessageConsumer implements MessageConsumer<String> {
        private Message<String> message;

        @Nonnull
        @Override
        public Class<String> payloadType() {
            return String.class;
        }

        @Nonnull
        @Override
        public Pattern keyPattern() {
            return Pattern.compile(".*");
        }

        @Override
        public void accept(Message<String> message) {
            this.message = message;
        }
    }
}