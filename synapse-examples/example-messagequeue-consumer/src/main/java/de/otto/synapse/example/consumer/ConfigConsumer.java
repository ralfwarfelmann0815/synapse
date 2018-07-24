package de.otto.synapse.example.consumer;

import de.otto.synapse.annotation.messagequeue.MessageQueueConsumer;
import de.otto.synapse.example.consumer.payload.BananaPayload;
import de.otto.synapse.example.consumer.payload.ProductPayload;
import de.otto.synapse.example.consumer.state.BananaProduct;
import de.otto.synapse.message.Message;
import de.otto.synapse.state.StateRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.otto.synapse.example.consumer.state.BananaProduct.bananaProductBuilder;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ConfigConsumer {

    private static final Logger LOG = getLogger(ConfigConsumer.class);

    private final StateRepository<BananaProduct> stateRepository;

    @Autowired
    public ConfigConsumer(StateRepository<BananaProduct> stateRepository) {
        this.stateRepository = stateRepository;
    }

    @MessageQueueConsumer(
            endpointName = "configQueue",
            payloadType = String.class
    )
    public void consumeConfigs(final Message<String> message) {
        System.out.println(message.getPayload());
        LOG.info("Received config: {}", message.toString());
    }



}
