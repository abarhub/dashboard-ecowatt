package org.ecowatt.dashboard.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ConfigProperties {

    private String urlOAuth2;
    private String urlEcowatt;
    private String secretKey;

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
}
