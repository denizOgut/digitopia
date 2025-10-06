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
    public static final String ORG_DELETED_QUEUE = "organization.deleted.queue";
    public static final String ORG_DELETED_KEY = "organization.deleted";

    public static final String INVITATION_EXCHANGE = "digitopia.invitation.exchange";
    public static final String INVITATION_ACCEPTED_QUEUE = "invitation.accepted.queue";
    public static final String INVITATION_ACCEPTED_KEY = "invitation.accepted";

    public static final String USER_EXCHANGE = "digitopia.user.exchange";
    public static final String USER_DELETED_QUEUE = "user.deleted.queue";
    public static final String USER_DELETED_KEY = "user.deleted";

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
    public Queue orgDeletedQueue() {
        return new Queue(ORG_DELETED_QUEUE, true);
    }

    @Bean
    public Binding orgDeletedBinding() {
        return BindingBuilder
            .bind(orgDeletedQueue())
            .to(orgExchange())
            .with(ORG_DELETED_KEY);
    }

    @Bean
    public TopicExchange invitationExchange() {
        return new TopicExchange(INVITATION_EXCHANGE);
    }

    @Bean
    public Queue invitationAcceptedConsumerQueue() {
        return new Queue(INVITATION_ACCEPTED_QUEUE, true);
    }

    @Bean
    public Binding invitationAcceptedBinding() {
        return BindingBuilder
            .bind(invitationAcceptedConsumerQueue())
            .to(invitationExchange())
            .with(INVITATION_ACCEPTED_KEY);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userDeletedConsumerQueue() {
        return new Queue(USER_DELETED_QUEUE, true);
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder
            .bind(userDeletedConsumerQueue())
            .to(userExchange())
            .with(USER_DELETED_KEY);
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
