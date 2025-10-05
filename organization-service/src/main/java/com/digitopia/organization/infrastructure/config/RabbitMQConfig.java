package com.digitopia.organization.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORG_EXCHANGE = "digitopia.organization.exchange";
    public static final String ORG_CREATED_QUEUE = "organization.created.queue";
    public static final String ORG_CREATED_KEY = "organization.created";

    @Bean
    public TopicExchange orgExchange() {
        return new TopicExchange(ORG_EXCHANGE);
    }

    @Bean
    public Queue orgCreatedQueue() {
        return new Queue(ORG_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orgCreatedBinding() {
        return BindingBuilder
            .bind(orgCreatedQueue())
            .to(orgExchange())
            .with(ORG_CREATED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
