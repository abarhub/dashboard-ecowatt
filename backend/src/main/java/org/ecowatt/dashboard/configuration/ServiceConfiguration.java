package org.ecowatt.dashboard.configuration;

import org.ecowatt.dashboard.properties.ConfigProperties;
import org.ecowatt.dashboard.service.*;
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
    public EcowattService ecowattService(ConfigProperties configProperties) {
        return new EcowattService(configProperties);
    }

    @Bean
    public MainService mainService(EcowattService ecowattService, CacheService cacheService) {
        return new MainService(ecowattService, cacheService);
    }

    @Bean
    public CacheService cacheService(ConfigProperties configProperties, FileService fileService) {
        return new CacheService(configProperties, fileService);
    }

    @Bean
    public SchedulerService schedulerService(MainService mainService, ConfigProperties configProperties) {
        LOGGER.atInfo().log("scheduling with '{}'", configProperties.getCronRechargement());
        return new SchedulerService(mainService);
    }

    @Bean
    public FileService fileService(ConfigProperties configProperties) {
        return new FileService(configProperties);
    }

}
