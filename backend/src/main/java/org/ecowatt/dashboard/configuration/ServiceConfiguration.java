package org.ecowatt.dashboard.configuration;

import org.ecowatt.dashboard.constantes.Constantes;
import org.ecowatt.dashboard.properties.ConfigProperties;
import org.ecowatt.dashboard.service.CacheService;
import org.ecowatt.dashboard.service.EcowattService;
import org.ecowatt.dashboard.service.MainService;
import org.ecowatt.dashboard.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
@EnableScheduling
public class ServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfiguration.class);

    @Bean
    public EcowattService ecowattService(ConfigProperties configProperties){
        return new EcowattService(configProperties);
    }

    @Bean
    public MainService mainService(EcowattService ecowattService, ConfigProperties configProperties,
                                   CacheService cacheService){
        return new MainService(ecowattService, configProperties, cacheService);
    }

    @Bean
    public CacheService cacheService(ConfigProperties configProperties){
        return new CacheService(configProperties);
    }

    @Bean
    public SchedulerService schedulerService(MainService mainService, ConfigProperties configProperties){
        LOGGER.atInfo().log("scheduling with '{}'", configProperties.getCronRechargement());
        return new SchedulerService(mainService);
    }

//    @Bean
//    WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {
//        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
//                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
//                        clientRegistrations,
//                        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager());
//
////        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
////                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
////                        clientRegistrations,
////                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
//        oauth.setDefaultClientRegistrationId("ecowatt");
//        return WebClient.builder()
//                .filter(oauth)
//                .build();
//    }

    @Bean
    ReactiveClientRegistrationRepository getRegistration(
            @Value("${spring.security.oauth2.client.provider."+Constantes.OAUTH_CLIENT+".token-uri}") String token_uri,
            @Value("${spring.security.oauth2.client.registration."+Constantes.OAUTH_CLIENT+".client-id}") String client_id,
            @Value("${spring.security.oauth2.client.registration."+Constantes.OAUTH_CLIENT+".client-secret}") String client_secret
    ) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(Constantes.OAUTH_CLIENT)
                .tokenUri(token_uri)
                .clientId(client_id)
                .clientSecret(client_secret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean//(name = "myprovider")
    WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {

        HttpClient httpClient = HttpClient
                .create()
                .wiretap(true);
        InMemoryReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(Constantes.OAUTH_CLIENT);
        //oauth.filter()
        return WebClient.builder()
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest3());
                    exchangeFilterFunctions.add(logResponseStatus());
                    exchangeFilterFunctions.add(oauth);
                })
                //.filter(logRequest2())
//                .filters(exchangeFilterFunctions -> {
//                    exchangeFilterFunctions.add(logRequest());
//                    exchangeFilterFunctions.add(logResponse());
//                })
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
                //.filter(oauth)
                .build();

    }
    private ExchangeFilterFunction logRequest2() {
        return (clientRequest, next) -> {
            LOGGER.info("Request2: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> LOGGER.info("headers {}={}", name, value)));
            //clientRequest.body().insert()
            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction logRequest3() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            LOGGER.info("Request3: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> LOGGER.info("header3 {}={}", name, value)));
            var res=clientRequest.body();
            LOGGER.atInfo().log("request body: {}",res);
//            var res = clientResponse.bodyToMono(String.class);
//            res.log()
//                    .subscribe((x)->{
//                        LOGGER.atInfo().log("response erreur: {}",x);
//                    });
            return Mono.just(clientRequest);
        });
    }
    private ExchangeFilterFunction logResponseStatus() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            LOGGER.info("Response3 Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request: \n");
                //append clientRequest method and url
                clientRequest
                        .headers()
                        .forEach((name, values) -> values.forEach(value -> sb.append(value)));
                LOGGER.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }

    ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Response: \n");
                //append clientRequest method and url
                clientResponse
                        .headers().asHttpHeaders()
                        .forEach((name, values) -> values.forEach(value ->sb.append(value)));
                LOGGER.debug(sb.toString());
            }
            return Mono.just(clientResponse);
        });
    }

}
