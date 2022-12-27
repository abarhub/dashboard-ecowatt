package org.ecowatt.dashboard.service;

import jakarta.annotation.PostConstruct;
import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.dto.web.HeureDto;
import org.ecowatt.dashboard.dto.web.JourneeDto;
import org.ecowatt.dashboard.dto.web.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class MainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainService.class);

    private final EcowattService ecowattService;


    private final CacheService cacheService;

    public MainService(EcowattService ecowattService, CacheService cacheService) {
        this.ecowattService = ecowattService;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        this.cacheService.loadCacheFromFile();
    }

    public Mono<DashboardDto> getEcowatt() {
        if (cacheService.isInvalide()) {
            LOGGER.info("Appel de EcoWatt");
            return ecowattService.getEcowatt()
                    .map(x -> {
                        var res = convertie(x);
                        cacheService.setCache(res);
                        return res;
                    })
                    .switchIfEmpty(Mono.justOrEmpty(cacheService.getCache()));
        } else {
            LOGGER.info("Recuperation du cache");
            return Mono.justOrEmpty(cacheService.getCache());
        }
    }

    private DashboardDto convertie(EcowattDto ecowattDto) {
        DashboardDto dashboardDto = new DashboardDto();
        if (ecowattDto != null && !CollectionUtils.isEmpty(ecowattDto.getSignals())) {
            dashboardDto.setListJournees(new ArrayList<>());
            for (var journee : ecowattDto.getSignals()) {
                if (dashboardDto.getDateEcowatt() == null && StringUtils.hasText(journee.getGenerationFichier())) {
                    try {
                        var zdt = ZonedDateTime.parse(journee.getGenerationFichier());
                        dashboardDto.setDateEcowatt(zdt.toLocalDateTime());
                    } catch (DateTimeParseException e) {
                        LOGGER.atError().setCause(e).addKeyValue("date", journee.getGenerationFichier())
                                .log("Erreur pour parser la date du fichier");
                    }
                }
                JourneeDto journeeDto = new JourneeDto();
                journeeDto.setMessage(journee.getMessage());
                journeeDto.setStatut(convertieStatut(journee.getDvalue()));
                if (StringUtils.hasText(journee.getJour())) {
                    var zdt = ZonedDateTime.parse(journee.getJour());
                    journeeDto.setDate(zdt.toLocalDate());
                }
                if (!CollectionUtils.isEmpty(journee.getValues())) {
                    journeeDto.setHeures(new ArrayList<>());
                    for (var heure : journee.getValues()) {
                        HeureDto heureDto = new HeureDto();
                        heureDto.setHeure(heure.getPas());
                        heureDto.setStatusEnum(convertieStatut(heure.getHvalue()));
                        journeeDto.getHeures().add(heureDto);
                    }
                }
                dashboardDto.getListJournees().add(journeeDto);
            }
        }
        return dashboardDto;
    }

    private StatusEnum convertieStatut(int statut) {
        return switch (statut) {
            case 1 -> StatusEnum.OK;
            case 2 -> StatusEnum.WARN;
            case 3 -> StatusEnum.KO;
            default -> throw new IllegalArgumentException("Statut '" + statut + "' invalide");
        };
    }
}
