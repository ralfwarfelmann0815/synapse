package de.otto.synapse.configuration.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.aws.configuration.AwsProperties;
import de.otto.synapse.annotation.messagequeue.MessageQueueConsumerBeanPostProcessor;
import de.otto.synapse.endpoint.MessageInterceptorRegistry;
import de.otto.synapse.endpoint.receiver.MessageQueueConsumerProcess;
import de.otto.synapse.endpoint.receiver.MessageQueueReceiverEndpoint;
import de.otto.synapse.endpoint.receiver.MessageQueueReceiverEndpointFactory;
import de.otto.synapse.endpoint.receiver.aws.SqsMessageQueueReceiverEndpoint;
import de.otto.synapse.endpoint.sender.aws.SqsMessageSenderEndpointFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SQSAsyncClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class SqsAutoConfiguration {

    @Autowired(required = false)
    private List<MessageQueueReceiverEndpoint> messageQueueReceiverEndpoints;

    private final AwsProperties awsProperties;

    @Autowired
    public SqsAutoConfiguration(final AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }

    @Bean
    @ConditionalOnMissingBean(SQSAsyncClient.class)
    public SQSAsyncClient sqsAsyncClient(final AwsCredentialsProvider credentialsProvider) {
        try {
            return SQSAsyncClient.builder()
                    .endpointOverride(new URI("https://sqs.eu-central-1.amazonaws.com/159193630992/"))
                    .credentialsProvider(credentialsProvider)
                    .region(Region.of(awsProperties.getRegion()))
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqsMessageSenderEndpointFactory sqsSenderEndpointFactory(final MessageInterceptorRegistry registry,
                                                                    final ObjectMapper objectMapper,
                                                                    final SQSAsyncClient sqsAsyncClient) {
        return new SqsMessageSenderEndpointFactory(registry, objectMapper, sqsAsyncClient);
    }

    // TODO Should return SqsMessageQueueReceiverEndpoint, @ConditionalOnMissingBean checks for existing MessageQueueReceiverEndpointFactory and skips SQS when Kinesis already registerd
    @Bean
    @ConditionalOnMissingBean
    public MessageQueueReceiverEndpointFactory sqsReceiverEndpointFactory(final MessageInterceptorRegistry registry,
                                                                          final ObjectMapper objectMapper,
                                                                          final SQSAsyncClient sqsAsyncClient,
                                                                          final ApplicationEventPublisher eventPublisher) {

        return (String channelName) -> {
            final SqsMessageQueueReceiverEndpoint endpoint = new SqsMessageQueueReceiverEndpoint(channelName, sqsAsyncClient, objectMapper, eventPublisher);
            endpoint.registerInterceptorsFrom(registry);
            return endpoint;
        };
    }


    @Bean
    @ConditionalOnProperty(
            prefix = "synapse",
            name = "consumer-process.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public MessageQueueConsumerProcess messageQueueConsumerProcess() {
        return new MessageQueueConsumerProcess(messageQueueReceiverEndpoints);
    }


    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    public MessageQueueConsumerBeanPostProcessor messageQueueConsumerAnnotationBeanPostProcessor() {
        return new MessageQueueConsumerBeanPostProcessor();
    }

}
