package com.example.titan_watch_learning_project.config;

//import com.azure.messaging.servicebus.ServiceBusClientBuilder;
//import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "webhook.queue.enabled", havingValue = "true")
public class ServiceBusConfig {

    public static final String WEBHOOK_QUEUE = "titan-watch-servicebus";

    @Value("${spring.jms.servicebus.connection-string}")
    private String connectionString;

    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {
        log.info("Azure Service Bus SenderClient creating...");
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(WEBHOOK_QUEUE)
                .buildClient();
    }
}