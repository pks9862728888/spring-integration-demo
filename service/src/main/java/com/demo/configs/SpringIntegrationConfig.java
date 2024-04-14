package com.demo.configs;

import com.demo.model.AttendeeRegistration;
import com.demo.service.RegistrationService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SpringIntegrationConfig {

    @Bean
    public MessageChannel fromRabbit() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel registrationRequest() {
        return new DirectChannel();
    }

    @Bean
    @Transformer(inputChannel = "fromRabbit", outputChannel = "registrationRequest")
    public JsonToObjectTransformer jsonToObjectTransformer() {
        return new JsonToObjectTransformer(AttendeeRegistration.class);
    }

    /**
     * A {@link org.springframework.amqp.rabbit.listener.MessageListenerContainer MessageListenerContainer} to receive messages from RabbitMQ.
     * <p>
     * Spring Boot automatically configures a {@link ConnectionFactory} for RabbitMQ because {@code org.springframework.amqp:spring-amqp}
     * and {@code org.springframework.amqp:spring-rabbit} are on the classpath.
     *
     * @param connectionFactory Connection factory to connect with RabbitMQ.
     * @return An {@link AbstractMessageListenerContainer}.
     */
    @Bean
    public AbstractMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer msgListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        msgListenerContainer.setQueueNames("globomantics.registrationRequest");
        return msgListenerContainer;
    }

    /**
     * An inbound channel adapter that receives messages from RabbitMQ using a message listener container and puts them on a Spring Integration channel.
     *
     * @param listenerContainer The message listener container.
     * @return An {@link AmqpInboundChannelAdapter}.
     */
    @Bean
    public AmqpInboundChannelAdapter inboundChannelAdapter(AbstractMessageListenerContainer listenerContainer) {
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setOutputChannelName("fromRabbit");
        return adapter;
    }

    @Bean
    public IntegrationFlow integrationFlow(RegistrationService registrationService) {
        return IntegrationFlows.from("registrationRequest")
                .handle(registrationService, "register")
                .get();
    }
}
