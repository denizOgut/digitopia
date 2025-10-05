package com.digitopia.invitation.infrastructure.config;

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

    public static final String INVITATION_EXCHANGE = "digitopia.invitation.exchange";
    public static final String INVITATION_ACCEPTED_QUEUE = "invitation.accepted.queue";
    public static final String INVITATION_ACCEPTED_KEY = "invitation.accepted";
    public static final String INVITATION_EXPIRED_QUEUE = "invitation.expired.queue";
    public static final String INVITATION_EXPIRED_KEY = "invitation.expired";

    @Bean
    public TopicExchange invitationExchange() {
        return new TopicExchange(INVITATION_EXCHANGE);
    }

    @Bean
    public Queue invitationAcceptedQueue() {
        return new Queue(INVITATION_ACCEPTED_QUEUE, true);
    }

    @Bean
    public Queue invitationExpiredQueue() {
        return new Queue(INVITATION_EXPIRED_QUEUE, true);
    }

    @Bean
    public Binding invitationAcceptedBinding() {
        return BindingBuilder
            .bind(invitationAcceptedQueue())
            .to(invitationExchange())
            .with(INVITATION_ACCEPTED_KEY);
    }

    @Bean
    public Binding invitationExpiredBinding() {
        return BindingBuilder
            .bind(invitationExpiredQueue())
            .to(invitationExchange())
            .with(INVITATION_EXPIRED_KEY);
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
