package org.ecowatt.dashboard.configuration;

import org.ecowatt.dashboard.properties.ConfigProperties;
import org.ecowatt.dashboard.service.EcowattService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class ServiceConfiguration {

    @Bean
    public EcowattService ecowattService(ConfigProperties configProperties){
        return new EcowattService(configProperties);
    }

}
