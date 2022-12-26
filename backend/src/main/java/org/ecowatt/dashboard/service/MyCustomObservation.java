package org.ecowatt.dashboard.service;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyCustomObservation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCustomObservation.class);

    private final ObservationRegistry observationRegistry;

    public MyCustomObservation(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public void doSomething() {
        LOGGER.info("doSomething ...");
        Observation.createNotStarted("doSomething", this.observationRegistry)
                .lowCardinalityKeyValue("locale", "en-US")
                .highCardinalityKeyValue("userId", "42")
                .observe(() -> {
                    // Execute business logic here
                    LOGGER.info("doSomething observe");
                });
        LOGGER.info("doSomething ok");
    }
}
