package org.ecowatt.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
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

    public void loadCacheFromFile(){
        var file=configProperties.getFile();
        if(StringUtils.hasText(file)){
            fileCache=Path.of(file).toAbsolutePath();
            if(Files.exists(fileCache)){
                try {
                    var dashboard = objectMapper.readValue(fileCache.toFile(), DashboardDto.class);
                    if(dashboard!=null&&!CollectionUtils.isEmpty(dashboard.getListJournees())){
                        lastDashboard=dashboard;
                    }
                }catch(Exception e){
                    LOGGER.error("Erreur pour lire le fichier "+fileCache, e);
                }
            }
        }
    }

    public void setCache(DashboardDto dashboard) {
        lastDashboard = dashboard;
        lastDashboard.setDate(LocalDateTime.now());
        if(fileCache!=null&&Files.exists(fileCache)) {
            try {
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
