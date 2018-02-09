package de.otto.edison.eventsourcing.example.producer;

import de.otto.edison.eventsourcing.configuration.ConsumerProcessProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"de.otto.edison"})
@EnableConfigurationProperties(ConsumerProcessProperties.class)
public class Server {
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
