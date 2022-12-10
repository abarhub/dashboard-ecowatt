package org.ecowatt.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class SchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

    private final MainService mainService;

    public SchedulerService(MainService mainService) {
        this.mainService = mainService;
    }

    @Scheduled(cron = "${app.cronRechargement}")
    public void task(){
        LOGGER.atInfo().log("Chargement des données (scheduling) ...");
        var ecowatt=mainService.getEcowatt().block();
        LOGGER.atInfo().log("res={}",ecowatt);
        LOGGER.atInfo().log("Chargement des données (scheduling) OK");
    }
}
