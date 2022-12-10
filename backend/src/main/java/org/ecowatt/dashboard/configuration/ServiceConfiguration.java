package org.ecowatt.dashboard.configuration;

import org.ecowatt.dashboard.properties.ConfigProperties;
import org.ecowatt.dashboard.service.CacheService;
import org.ecowatt.dashboard.service.EcowattService;
import org.ecowatt.dashboard.service.MainService;
import org.ecowatt.dashboard.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
@EnableScheduling
public class ServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfiguration.class);

    @Bean
    public EcowattService ecowattService(ConfigProperties configProperties){
        return new EcowattService(configProperties);
    }

    @Bean
    public MainService mainService(EcowattService ecowattService, ConfigProperties configProperties,
                                   CacheService cacheService){
        return new MainService(ecowattService, configProperties, cacheService);
    }

    @Bean
    public CacheService cacheService(ConfigProperties configProperties){
        return new CacheService(configProperties);
    }

    @Bean
    public SchedulerService schedulerService(MainService mainService, ConfigProperties configProperties){
        LOGGER.atInfo().log("scheduling with '{}'", configProperties.getCronRechargement());
        return new SchedulerService(mainService);
    }
}
