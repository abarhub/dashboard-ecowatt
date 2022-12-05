package org.ecowatt.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final ConfigProperties configProperties;

    private final ObjectMapper objectMapper;

    private DashboardDto lastDashboard;

    private Path fileCache;

    public CacheService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    public void loadCacheFromFile() {
        var file = configProperties.getFile();
        if (StringUtils.hasText(file)) {
            fileCache = Path.of(file).toAbsolutePath();
            if (Files.exists(fileCache)) {
                try {
                    var dashboard = objectMapper.readValue(fileCache.toFile(), DashboardDto.class);
                    if (dashboard != null && !CollectionUtils.isEmpty(dashboard.getListJournees())) {
                        lastDashboard = dashboard;
                    }
                } catch (Exception e) {
                    LOGGER.error("Erreur pour lire le fichier " + fileCache, e);
                }
            }
        }
    }

    public void setCache(DashboardDto dashboard) {
        lastDashboard = dashboard;
        lastDashboard.setDate(LocalDateTime.now());
        if (fileCache != null) {
            try {
                if (Files.exists(fileCache)) {
                    int i = 2;
                    Path p = fileCache.getParent().resolve(fileCache.getFileName() + "_" + i);
                    while (Files.exists(p)) {
                        i++;
                        p = fileCache.getParent().resolve(fileCache.getFileName() + "_" + i);
                    }
                    if (Files.notExists(p)) {
                        LOGGER.atInfo().log("Renomage du fichier '{}' vers '{}'", fileCache, p);
                        Files.move(fileCache, p);
                    }
                }
                LOGGER.info("Ecriture du fichier {}", fileCache);
                objectMapper.writeValue(fileCache.toFile(), lastDashboard);
            } catch (Exception e) {
                LOGGER.error("Erreur pour écrire le fichier " + fileCache, e);
            }
        }
    }

    public DashboardDto getCache() {
        return lastDashboard;
    }

    public boolean isInvalide() {
        return lastDashboard == null || lastDashboard.getDate() == null ||
                lastDashboard.getDate().plus(configProperties.getDureeCache()).isBefore(LocalDateTime.now());
    }

}
