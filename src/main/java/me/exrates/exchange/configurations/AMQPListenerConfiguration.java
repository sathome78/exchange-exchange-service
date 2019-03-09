package me.exrates.exchange.configurations;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AMQPListenerConfiguration {

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             Queue queue,
                                             MessageListener listener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(queue);
        container.setMessageListener(listener);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        return container;
    }
}