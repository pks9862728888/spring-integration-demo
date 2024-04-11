package com.demo.config;

import com.demo.service.RegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SpringIntegrationConfig {

    public static final String REGISTRATION_REQUEST = "registrationRequest";

    @Bean
    public MessageChannel registrationRequest() {
        return MessageChannels.direct(REGISTRATION_REQUEST).get();
    }

    @Bean
    public IntegrationFlow integrationFlow(RegistrationService registrationService) {
        return IntegrationFlows.from(REGISTRATION_REQUEST)
                .handle(registrationService, "register")
                .get();
    }
}
