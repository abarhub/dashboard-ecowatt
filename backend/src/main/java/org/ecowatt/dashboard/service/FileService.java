package org.ecowatt.dashboard.service;

import io.github.abarhub.vfs.core.api.VFS4JDefaultFileManager;
import io.github.abarhub.vfs.core.api.VFS4JParseConfigFile;
import io.github.abarhub.vfs.core.api.path.VFS4JPathName;
import io.github.abarhub.vfs.core.api.path.VFS4JPaths;
import jakarta.annotation.PostConstruct;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final ConfigProperties configProperties;

    private VFS4JPathName pathName;

    private VFS4JPathName backupPathName;

    public FileService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @PostConstruct
    public void init() throws IOException {
        LOGGER.atInfo().log("Initialisation de VFS4J ...");
        // chargelement de la configuration initiale pour pouvoir la recharger apres
        VFS4JDefaultFileManager.get();
        // initialisation de la configuration
        LOGGER.atInfo().log("Reconfiguration de VFS4J");
        Path p = Path.of(configProperties.getPath());
        if (!Files.exists(p)) {
            throw new IOException("Le répertoire '" + p + "' n'existe pas");
        }
        if (!Files.isDirectory(p)) {
            throw new IOException("Le chemin '" + p + "' n'est pas un répertoire");
        }
        Properties properties = new Properties();
        properties.put("vfs.paths.rep.path", p.toAbsolutePath().normalize().toString());
        if (!CollectionUtils.isEmpty(configProperties.getVfs4jProperties())) {
            properties.putAll(configProperties.getVfs4jProperties());
        }
        LOGGER.atDebug().log("vfs4 properties: {}", properties);
        VFS4JParseConfigFile parseConfigFile = new VFS4JParseConfigFile();
        var config = parseConfigFile.parse(properties).build();
        VFS4JDefaultFileManager.get().setConfig(config);
        pathName = VFS4JPaths.get("rep", configProperties.getFilename());
        backupPathName = VFS4JPaths.get("rep", "backup");
        LOGGER.atInfo().log("Initialisation de VFS4J OK");
    }

    public VFS4JPathName getFile() {
        return pathName;
    }

    public VFS4JPathName getBackupDirectory() {
        return backupPathName;
    }
}
