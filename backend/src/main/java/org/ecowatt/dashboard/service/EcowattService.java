package org.ecowatt.dashboard.service;

import io.netty.handler.logging.LogLevel;
import org.ecowatt.dashboard.dto.rte.AccessKeyDto;
import org.ecowatt.dashboard.dto.rte.EcowattDto;
import org.ecowatt.dashboard.dto.web.DashboardDto;
import org.ecowatt.dashboard.dto.web.HeureDto;
import org.ecowatt.dashboard.dto.web.JourneeDto;
import org.ecowatt.dashboard.dto.web.StatusEnum;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;

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
//                .create(this.configProperties.getUrlOAuth2());
        clientEcowatt = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .filters(exchangeFilterFunctions -> {
//                    exchangeFilterFunctions.add(logRequest());
//                    exchangeFilterFunctions.add(logResponse());
//                })
                .baseUrl(this.configProperties.getUrlEcowatt())
                .build();
    }

    public Mono<DashboardDto> getEcowatt() {
        var uriSpec = clientOAuth2.post();
        WebClient.RequestBodySpec bodySpec = uriSpec.uri("");
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("");
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
//                .ifNoneMatch("*")
//                .ifModifiedSince(ZonedDateTime.now())
                .header("Authorization", "Basic " + configProperties.getSecretKey())
                .retrieve();
        LOGGER.info("res2={}", responseSpec);
        var res1 = responseSpec.bodyToMono(AccessKeyDto.class)
//                .map(x->{
//                    EcowattDto ecowattDto=new EcowattDto();
//                    ecowattDto.setS(x.getAccessToken());
//                    return ecowattDto;
//                });
                .flatMap(x -> {
                    LOGGER.info("res0={}", x);
                    if (x.getAccess_token() != null) {
                        LOGGER.info("appel getWeb");
                        var res = getWeb(x.getAccess_token());
                        LOGGER.info("resultat getWeb={}", res);
                        return res;
                    } else {
                        LOGGER.info("empty");
                        return Mono.just(new DashboardDto());
                    }
                });

//        if(res1.)

        LOGGER.info("res={}", res1);
        return res1;
    }

    private Mono<DashboardDto> getWeb(String accessToken) {
        LOGGER.info("getWeb");
        var uriSpec = clientEcowatt.get();
//        WebClient.RequestBodySpec bodySpec = uriSpec.uri("");
        WebClient.RequestHeadersSpec<?> headersSpec = uriSpec;
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
//                .ifNoneMatch("*")
//                .ifModifiedSince(ZonedDateTime.now())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve();
        var res1 = responseSpec.bodyToMono(EcowattDto.class)
//                .map(x->{
//                    LOGGER.info("ecowattDto x={}",x);
//                    EcowattDto ecowattDto=new EcowattDto();
//                    ecowattDto.setS("XXX");
//                    LOGGER.info("ecowattDto={}",ecowattDto);
//                    return ecowattDto;
//                })
                .map(x -> {
                    LOGGER.info("ecowattDto={}", x);
                    return x;
                })
                .map(x -> {
                    return convertie(x);
                });
        ;
        LOGGER.info("res1={}", res1);
        return res1;
    }

    private DashboardDto convertie(EcowattDto ecowattDto) {
        DashboardDto dashboardDto = new DashboardDto();
        if (ecowattDto != null && !CollectionUtils.isEmpty(ecowattDto.getSignals())) {
            dashboardDto.setListJournees(new ArrayList<>());
            for (var journee : ecowattDto.getSignals()) {
                JourneeDto journeeDto = new JourneeDto();
                journeeDto.setMessage(journee.getMessage());
                journeeDto.setStatut(convertieStatut(journee.getDvalue()));
                if (StringUtils.hasText(journee.getJour())) {
                    var zdt = ZonedDateTime.parse(journee.getJour());
                    journeeDto.setDate(zdt.toLocalDate());
                }
                if (!CollectionUtils.isEmpty(journee.getValues())) {
                    journeeDto.setHeures(new ArrayList<>());
                    for (var heure : journee.getValues()) {
                        HeureDto heureDto = new HeureDto();
                        heureDto.setHeure(heure.getPas());
                        heureDto.setStatusEnum(convertieStatut(heure.getHvalue()));
                        journeeDto.getHeures().add(heureDto);
                    }
                }
                dashboardDto.getListJournees().add(journeeDto);
            }
        }
        return dashboardDto;
    }

    private StatusEnum convertieStatut(int statut) {
        switch (statut) {
            case 1:
                return StatusEnum.OK;
            case 2:
                return StatusEnum.WARN;
            case 3:
                return StatusEnum.KO;
            default:
                throw new IllegalArgumentException("Statut '" + statut + "' invalide");
        }
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n");
                //append clientRequest method and url
                clientRequest
                        .headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append(value + ";")));
                LOGGER.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofRequestProcessor(clientResponse -> {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Response: \n");
                //append clientRequest method and url
                clientResponse
                        .headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append(value + ";")));
                LOGGER.debug(sb.toString());
            }
            return Mono.just(clientResponse);
        });
    }

}
