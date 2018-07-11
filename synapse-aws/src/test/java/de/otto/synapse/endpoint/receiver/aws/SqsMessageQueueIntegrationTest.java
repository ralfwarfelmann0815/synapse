package de.otto.synapse.endpoint.receiver.aws;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.synapse.configuration.aws.TestMessageInterceptor;
import de.otto.synapse.consumer.MessageConsumer;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import de.otto.synapse.message.Message;
import de.otto.synapse.testsupport.SqsChannelSetupUtils;
import de.otto.synapse.testsupport.SqsTestStreamSource;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.sqs.SQSAsyncClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.synchronizedList;
import static java.util.Collections.synchronizedSet;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.otto.synapse"})
@SpringBootTest(classes = SqsMessageQueueIntegrationTest.class)
public class SqsMessageQueueIntegrationTest {

    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{});
    private static final int EXPECTED_NUMBER_OF_ENTRIES_IN_FIRST_SET = 10;
    private static final int EXPECTED_NUMBER_OF_ENTRIES_IN_SECOND_SET = 10;
    private static final String TEST_CHANNEL = "synapse-test-channel-2";

    @Autowired
    private SQSAsyncClient sqsAsyncClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageInterceptorRegistry messageInterceptorRegistry;

    @Autowired
    private TestMessageInterceptor testMessageInterceptor;

    private List<Message<String>> messages = synchronizedList(new ArrayList<>());
    private Set<String> threads = synchronizedSet(new HashSet<>());
    private SqsMessageQueueReceiverEndpoint sqsMessageQueue;

    @Before
    public void before() {
        messages.clear();
    }

    @PostConstruct
    public void setup() throws IOException {
        SqsChannelSetupUtils.createChannelIfNotExists(sqsAsyncClient, TEST_CHANNEL);

        /* We have to setup the EventSource manually, because otherwise the stream created above is not yet available
           when initializing it via @EnableEventSource
         */
        sqsMessageQueue = new SqsMessageQueueReceiverEndpoint(TEST_CHANNEL, sqsAsyncClient, objectMapper, null);
        sqsMessageQueue.registerInterceptorsFrom(messageInterceptorRegistry);
        sqsMessageQueue.register(MessageConsumer.of(".*", String.class, (message) -> {
            messages.add(message);
            threads.add(Thread.currentThread().getName());
        }));
    }

    @Test
    public void consumeDataFromSqs() throws ExecutionException, InterruptedException {
        // when
        writeToStream("users_small1.txt");

        // then
        sqsMessageQueue.consume();

        await()
                .atMost(Duration.FIVE_SECONDS)
                .until(() -> messages.size() >= EXPECTED_NUMBER_OF_ENTRIES_IN_FIRST_SET);
        sqsMessageQueue.stop();
    }

    @Test
    public void registerInterceptorAndInterceptMessages() throws ExecutionException, InterruptedException {
        // when
        testMessageInterceptor.clear();
        writeToStream("users_small1.txt");

        // then
        sqsMessageQueue.consume();

        await()
                .atMost(Duration.FIVE_SECONDS)
                .until(() -> testMessageInterceptor.getInterceptedMessages().size() == EXPECTED_NUMBER_OF_ENTRIES_IN_FIRST_SET);
    }

    private SqsTestStreamSource writeToStream(String filename) {
        SqsTestStreamSource streamSource = new SqsTestStreamSource(sqsAsyncClient, TEST_CHANNEL, filename);
        streamSource.writeToStream();
        return streamSource;
    }

}