package org.ecowatt.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ecowatt.dashboard.dto.fichier.Fichier;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final ConfigProperties configProperties;

    private final ObjectMapper objectMapper;

    private Fichier fichier;

    private Path fileCache;

    public CacheService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        fichier = new Fichier();
        fichier.setUrl(configProperties.getUrlEcowatt());
    }

    public void loadCacheFromFile() {
        var file = configProperties.getFile();
        if (StringUtils.hasText(file)) {
            fileCache = Path.of(file).toAbsolutePath();
            if (Files.exists(fileCache)) {
                try {
                    var fichier = objectMapper.readValue(fileCache.toFile(), Fichier.class);
                    if (fichier != null) {
                        this.fichier = fichier;
                    }
                } catch (Exception e) {
                    LOGGER.error("Erreur pour lire le fichier " + fileCache, e);
                }
            }
        }
    }

    public void setCache(DashboardDto dashboard) {
        var dte = LocalDateTime.now();
        fichier.setDashboardDto(dashboard);
        dashboard.setDate(dte);
        fichier.setLastUpdate(dte);
        fichier.setUrl(configProperties.getUrlEcowatt());
        if (fileCache != null) {
            try {
                if (Files.exists(fileCache)) {
                    int i = 1;
                    Path rootPath = fileCache.getParent().resolve("backup");
                    if (Files.notExists(rootPath)) {
                        Files.createDirectories(rootPath);
                    }
                    Path p = rootPath.resolve(fileCache.getFileName() + "_" + i);
                    while (Files.exists(p)) {
                        i++;
                        p = rootPath.resolve(fileCache.getFileName() + "_" + i);
                    }
                    if (Files.notExists(p)) {
                        LOGGER.atInfo().log("Renomage du fichier '{}' vers '{}'", fileCache, p);
                        Files.move(fileCache, p);
                    }
                }
                LOGGER.info("Ecriture du fichier {}", fileCache);
                objectMapper.writeValue(fileCache.toFile(), fichier);
            } catch (Exception e) {
                LOGGER.error("Erreur pour écrire le fichier " + fileCache, e);
            }
        }
    }

    public DashboardDto getCache() {
        return fichier.getDashboardDto();
    }

    public boolean isInvalide() {
        return fichier.getDashboardDto() == null || fichier.getDashboardDto().getDate() == null ||
                fichier.getDashboardDto().getDate().plus(configProperties.getDureeCache()).isBefore(LocalDateTime.now());
    }

}
