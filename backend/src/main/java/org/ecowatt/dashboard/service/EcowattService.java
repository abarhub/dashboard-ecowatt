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

public class EcowattService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EcowattService.class);

    private final ConfigProperties configProperties;

    private final WebClient clientOAuth2;

    private final WebClient clientEcowatt;

    public EcowattService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        HttpClient httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        clientOAuth2 = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(this.configProperties.getUrlOAuth2())
                .build();
        clientEcowatt = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(this.configProperties.getUrlEcowatt())
                .build();
    }

    public Mono<EcowattDto> getEcowatt() {
        var uriSpec = clientOAuth2.post();
        WebClient.RequestBodySpec bodySpec = uriSpec.uri("");
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("");
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .header("Authorization", "Basic " + configProperties.getSecretKey())
                .retrieve();
        LOGGER.info("res2={}", responseSpec);
        var res1 = responseSpec.bodyToMono(AccessKeyDto.class)
                .flatMap(x -> {
                    LOGGER.info("res0={}", x);
                    if (x.getAccess_token() != null) {
                        LOGGER.info("appel getWeb");
                        var res = getWeb(x.getAccess_token());
                        LOGGER.info("resultat getWeb={}", res);
                        return res;
                    } else {
                        LOGGER.info("empty");
                        Mono<EcowattDto> res = Mono.empty();
                        return res;
                    }
                });

        LOGGER.info("res={}", res1);
        return res1;
    }

    private Mono<EcowattDto> getWeb(String accessToken) {
        LOGGER.info("getWeb");
        WebClient.RequestHeadersSpec<?> headersSpec = clientEcowatt.get();
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                        error -> {
                            LOGGER.atWarn().log("too many request (code={})", error);
                            return Mono.empty();
                        });
        var res1 = responseSpec.bodyToMono(EcowattDto.class)
                .map(x -> {
                    LOGGER.info("ecowattDto={}", x);
                    return x;
                });
        ;
        LOGGER.info("res1={}", res1);
        return res1;
    }


}
