package org.ecowatt.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abarhub.vfs.core.api.VFS4JFiles;
import io.github.abarhub.vfs.core.api.path.VFS4JPathName;
import io.github.abarhub.vfs.core.api.path.VFS4JPaths;
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

    private VFS4JPathName fileCache;

    private final FileService fileService;

    public CacheService(ConfigProperties configProperties, FileService fileService) {
        this.configProperties = configProperties;
        this.fileService=fileService;
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        fichier = new Fichier();
        fichier.setUrl(configProperties.getUrlEcowatt());
    }

    public void loadCacheFromFile() {
        var file=fileService.getFile();
        if(file!=null) {
            fileCache = file;
        }
//        var file = configProperties.getFile();
//        if (StringUtils.hasText(file)) {
        if(fileCache!=null&&VFS4JFiles.exists(fileCache)){
//            if (Files.exists(fileCache)) {
                try(var reader=VFS4JFiles.newReader(fileCache)) {
                    var fichier = objectMapper.readValue(reader, Fichier.class);
                    if (fichier != null) {
                        this.fichier = fichier;
                    }
                } catch (Exception e) {
                    LOGGER.error("Erreur pour lire le fichier " + fileCache, e);
                }
//            }
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
                if (VFS4JFiles.exists(fileCache)) {
                    int i = 1;
                    var rootPath = fileService.getBackupDirectory();
                    if (VFS4JFiles.notExists(rootPath)) {
                        VFS4JFiles.createDirectories(rootPath);
                    }
                    var p = rootPath.resolve(fileCache.getFilename() + "_" + i);
                    while (VFS4JFiles.exists(p)) {
                        i++;
                        p = rootPath.resolve(fileCache.getFilename() + "_" + i);
                    }
                    if (VFS4JFiles.notExists(p)) {
                        LOGGER.atInfo().log("Renomage du fichier '{}' vers '{}'", fileCache, p);
                        VFS4JFiles.move(fileCache, p);
                    }
                }
                LOGGER.info("Ecriture du fichier {}", fileCache);
                try(var writer= VFS4JFiles.newWriter(fileCache,false)) {
                    objectMapper.writeValue(writer, fichier);
                }
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
