package org.ecowatt.dashboard.service;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class EcowattObservation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EcowattObservation.class);

    private final ObservationRegistry observationRegistry;

    public EcowattObservation(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public void observe(EcowattDto x) {
        LOGGER.info("observe ...");
        KeyValues tmp=null;
        LocalDate date2=null;

        for(var tmp2:x.getSignals()){
            var t=tmp2.getDvalue();
            var s=tmp2.getJour();
            LocalDateTime dte=null;
            LocalDate dte2=null;
            if ( StringUtils.hasText(tmp2.getJour())) {
                try {
                    var zdt = ZonedDateTime.parse(tmp2.getJour());
                    dte=zdt.toLocalDateTime();
                    dte2=dte.toLocalDate();
                } catch (DateTimeParseException e) {
                    LOGGER.atError().setCause(e)
                            .log("Erreur pour parser la date du fichier");
                }
            }
            if(dte2!=null){
                if(tmp==null){
                    tmp=KeyValues.of("date0",t+"");
                    date2=dte2;
                } else {
                    var tmp3= ChronoUnit.DAYS.between(dte2,date2);
                    tmp=tmp.and(KeyValue.of("date"+tmp3,t+""));
                }
            }
        }
//        tmp=tmp.and(KeyValue.of("generation",t+""));
        var tmp4=tmp;
        Observation.createNotStarted("ecowatt", this.observationRegistry)
                .lowCardinalityKeyValue("locale", "en-US")
                .highCardinalityKeyValue("userId", "42")
                .lowCardinalityKeyValues(tmp)
                .observe(() -> {
                    // Execute business logic here
                    LOGGER.info("ecowat observe: {}",tmp4);
                });
        LOGGER.info("observe ok");
    }
}
