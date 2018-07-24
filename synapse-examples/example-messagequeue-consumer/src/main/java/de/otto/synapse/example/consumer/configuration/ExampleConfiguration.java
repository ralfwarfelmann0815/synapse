package de.otto.synapse.example.consumer.configuration;

import de.otto.synapse.annotation.messagequeue.EnableMessageQueueReceiverEndpoint;
import de.otto.synapse.configuration.MessageEndpointConfigurer;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import de.otto.synapse.endpoint.sender.MessageSenderEndpoint;
import de.otto.synapse.endpoint.sender.aws.SqsMessageSenderEndpointFactory;
import de.otto.synapse.example.consumer.state.BananaProduct;
import de.otto.synapse.state.ConcurrentHashMapStateRepository;
import de.otto.synapse.state.StateRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SQSAsyncClient;

import static de.otto.synapse.endpoint.MessageInterceptorRegistration.receiverChannelsWith;
import static de.otto.synapse.endpoint.MessageInterceptorRegistration.senderChannelsWith;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableConfigurationProperties({MyServiceProperties.class})
@EnableMessageQueueReceiverEndpoint(name = "bananaQueue",  channelName = "${exampleservice.banana-channel}")
@EnableMessageQueueReceiverEndpoint(name = "productQueue", channelName = "${exampleservice.product-channel}")
public class ExampleConfiguration implements MessageEndpointConfigurer {


    private static final Logger LOG = getLogger(ExampleConfiguration.class);

    @Autowired
    private SQSAsyncClient sqsAsyncClient;

    @Override
    public void configureMessageInterceptors(final MessageInterceptorRegistry registry) {
        registry.register(receiverChannelsWith(m -> {
            LOG.info("[receiver] Intercepted message {}", m);
            return m;
        }));
        registry.register(senderChannelsWith((m) -> {
            LOG.info("[sender] Intercepted message {}", m);
            return m;
        }));
    }

//    @Bean
//    public SQSAsyncClient sqsAsyncClient() {
//        return SQSAsyncClient.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsCredentials.create("foobar", "foobar")))
//                .endpointOverride(URI.create("http://localhost:4576"))
//                .build();
//    }

    @Bean
    public StateRepository<BananaProduct> bananaProductConcurrentStateRepository() {
        return new ConcurrentHashMapStateRepository<>();
    }

    @Bean
    public MessageSenderEndpoint configMessageSender(final SqsMessageSenderEndpointFactory sqsMessageSenderEndpointFactory,
                                                     final MyServiceProperties properties) {
        return sqsMessageSenderEndpointFactory.create(properties.getConfigChannel());
    }

    @Bean
    public MessageSenderEndpoint productMessageSender(final SqsMessageSenderEndpointFactory sqsMessageSenderEndpointFactory,
                                                      final MyServiceProperties properties) {
        return sqsMessageSenderEndpointFactory.create(properties.getProductChannel());
    }

}

