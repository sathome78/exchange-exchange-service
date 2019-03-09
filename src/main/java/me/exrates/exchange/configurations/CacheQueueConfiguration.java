package me.exrates.exchange.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for RabbitMQ
 */
@Slf4j
@EnableRabbit
@Configuration
public class CacheQueueConfiguration {

    @Value("${queues.queue}")
    protected String queueName;

    @Value("${queues.host}")
    protected String host;

    @Value("${queues.port}")
    protected int port;

    @Value("${queues.username}")
    protected String userName;

    @Value("${queues.password}")
    protected String password;

    @Value("${queues.exchange}")
    protected String exchange;

    @Value("${queues.key}")
    protected String key;

    @Bean
    public ConnectionFactory connectionFactory() {
        log.info("amqp: {}:{}, {}", host, port, userName);
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    DirectExchange rubeExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue rubeQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    Binding rubeExchangeBinding(DirectExchange rubeExchange, Queue rubeQueue) {
        return BindingBuilder.bind(rubeQueue).to(rubeExchange).with(key);
    }

    @Bean
    public RabbitTemplate rubeExchangeTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(exchange);
        template.setQueue(queueName);
        template.setRoutingKey(key);
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}