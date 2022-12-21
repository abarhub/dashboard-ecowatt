package org.ecowatt.dashboard.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    private String urlOAuth2;

    private String urlEcowatt;

    private String secretKey;

    private String path;

    private String filename;

    private Duration dureeCache;

    private String cronRechargement;

    private Map<String,String> vfs4jProperties;

    public String getUrlOAuth2() {
        return urlOAuth2;
    }

    public void setUrlOAuth2(String urlOAuth2) {
        this.urlOAuth2 = urlOAuth2;
    }

    public String getUrlEcowatt() {
        return urlEcowatt;
    }

    public void setUrlEcowatt(String urlEcowatt) {
        this.urlEcowatt = urlEcowatt;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Duration getDureeCache() {
        return dureeCache;
    }

    public void setDureeCache(Duration dureeCache) {
        this.dureeCache = dureeCache;
    }

    public String getCronRechargement() {
        return cronRechargement;
    }

    public void setCronRechargement(String cronRechargement) {
        this.cronRechargement = cronRechargement;
    }

    public Map<String, String> getVfs4jProperties() {
        return vfs4jProperties;
    }

    public void setVfs4jProperties(Map<String, String> vfs4jProperties) {
        this.vfs4jProperties = vfs4jProperties;
    }
}
