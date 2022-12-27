package org.ecowatt.dashboard.service;

import io.netty.handler.logging.LogLevel;
import org.ecowatt.dashboard.dto.rte.AccessKeyDto;
import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class EcowattService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EcowattService.class);

    private final ConfigProperties configProperties;

    private final WebClient clientOAuth2;

    private final WebClient clientEcowatt;

    private final EcowattObservation ecowattObservation;

    private Instant cacheDuree;
    private String token;

    public EcowattService(ConfigProperties configProperties,
                          WebClient.Builder webClientBuilder,
                          EcowattObservation ecowattObservation) {
        this.configProperties = configProperties;
        this.ecowattObservation=ecowattObservation;
        HttpClient httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        clientOAuth2 = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(this.configProperties.getUrlOAuth2())
                .build();
        clientEcowatt = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(this.configProperties.getUrlEcowatt())
                .build();
    }

    public Mono<EcowattDto> getEcowatt() {

        return getToken()
                .flatMap(x -> {
                    var res = getWeb(x);
                    LOGGER.info("resultat getWeb={}", res);
                    return res;
                });
    }

    private Mono<String> getToken() {
        if (cacheDuree == null || cacheDuree.isBefore(Instant.now())) {
            LOGGER.info("Récupération du token");
            return clientOAuth2.post()
                    .uri("")
                    .bodyValue("")
                    .header(
                            HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .header("Authorization", "Basic " + configProperties.getSecretKey())
                    .retrieve()
                    .bodyToMono(AccessKeyDto.class)
                    .flatMap(x -> {
                        LOGGER.debug("res0={}", x);
                        if (x.getAccess_token() != null) {
                            LOGGER.info("token récupéré");
                            var t = x.getExpires_in();
                            LOGGER.atDebug().log("Token valide pour {} ms", t);
                            if (t > 0) {
                                cacheDuree = Instant.now().plus(t, ChronoUnit.MILLIS);
                                token = x.getAccess_token();
                            }
                            return Mono.just(x.getAccess_token());
                        } else {
                            return Mono.empty();
                        }
                    });
        } else {
            LOGGER.info("get token en cache");
            return Mono.just(token);
        }
    }

    private Mono<EcowattDto> getWeb(String accessToken) {
        LOGGER.info("Récupération des données d'Ecowatt");
        return clientEcowatt.get()
                .header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                        error -> {
                            LOGGER.atWarn().log("too many request (code={})", error);
                            return Mono.empty();
                        })
                .bodyToMono(EcowattDto.class)
                .map(x -> {
                    LOGGER.info("ecowattDto={}", x);
                    ecowattObservation.observe(x);
                    return x;
                });
    }


}
