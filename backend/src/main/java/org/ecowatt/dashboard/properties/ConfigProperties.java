package org.ecowatt.dashboard.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    private String urlOAuth2;

    private String urlEcowatt;

    private String secretKey;

    private String file;

    private Duration dureeCache;

    private String cronRechargement;

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

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
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
}
