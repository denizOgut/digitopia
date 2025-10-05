package com.digitopia.user.infrastructure.config;

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

    public static final String USER_EXCHANGE = "digitopia.user.exchange";
    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_CREATED_KEY = "user.created";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
            .bind(userCreatedQueue())
            .to(userExchange())
            .with(USER_CREATED_KEY);
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
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
