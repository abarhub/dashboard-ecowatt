package org.ecowatt.dashboard.service;

import jakarta.annotation.PostConstruct;
import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.dto.web.HeureDto;
import org.ecowatt.dashboard.dto.web.JourneeDto;
import org.ecowatt.dashboard.dto.web.StatusEnum;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class MainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainService.class);

    private final EcowattService ecowattService;

//    private DashboardDto lastDashboard;
//    private LocalDateTime dateDernierAppel;

    private final ConfigProperties configProperties;

    private final CacheService cacheService;

    public MainService(EcowattService ecowattService, ConfigProperties configProperties,
                       CacheService cacheService) {
        this.ecowattService = ecowattService;
        this.configProperties = configProperties;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init(){
        this.cacheService.loadCacheFromFile();
    }

    public Mono<DashboardDto> getEcowatt() {
        Path p = Path.of("toto.txt");
        LOGGER.info("p={}", p.toAbsolutePath());
        LOGGER.info("f={}", configProperties.getFile());
        if (cacheService.isInvalide()) {
            LOGGER.info("Appel de EcoWatt");
            return ecowattService.getEcowatt().map(x -> {
                var res = convertie(x);
//                lastDashboard = res;
//                dateDernierAppel = LocalDateTime.now();
                cacheService.setCache(res);
                return res;
            });
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
