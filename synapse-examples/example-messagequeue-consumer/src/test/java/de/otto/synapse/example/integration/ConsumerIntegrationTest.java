package de.otto.synapse.example.integration;

import org.awaitility.Awaitility;
import de.otto.synapse.endpoint.sender.MessageSenderEndpoint;
import de.otto.synapse.endpoint.sender.MessageSenderEndpointFactory;
import de.otto.synapse.example.consumer.Server;
import de.otto.synapse.example.consumer.configuration.MyServiceProperties;
import de.otto.synapse.example.consumer.payload.BananaPayload;
import de.otto.synapse.example.consumer.state.BananaProduct;
import de.otto.synapse.state.StateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.sqs.SQSAsyncClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.otto.synapse.message.Message.message;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(classes = {Server.class})
@ActiveProfiles("test")
public class ConsumerIntegrationTest {

    @Autowired
    StateRepository<BananaProduct> bananaProductStateRepository;

    @Autowired
    MyServiceProperties properties;

    @Autowired
    @Qualifier("bananaMessageSender")
    private MessageSenderEndpoint bananaMessageSender;

    @Test
    public void shouldRetrieveBananasFromStream() {
        // given
        BananaPayload bananaPayload = new BananaPayload();
        bananaPayload.setId("");
        bananaPayload.setColor("yellow");

        // when
        bananaMessageSender.send(message("", bananaPayload)); // no message keys allowed in sqs

        // then
        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> bananaProductStateRepository.get("").isPresent());

        Optional<BananaProduct> optionalBananaProduct = bananaProductStateRepository.get("");
        assertThat(optionalBananaProduct.map(BananaProduct::getColor), is(Optional.of("yellow")));
    }
}