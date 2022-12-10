package org.ecowatt.dashboard.configuration;

import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
//@EnableWebSecurity
//@EnableWebFluxSecurity()
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                //.authorizeExchange()
//                //.authorizeRequests().antMatchers("/**").permitAll(); // config to permit all requests
//                .authorizeHttpRequests((authorize) ->
//                        authorize.anyRequest().permitAll());
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager() { // to delete default username and password that is printed in the log every time, you can provide here any auth manager (InMemoryAuthenticationManager, etc) as you need
//        return authentication -> {
//            throw new UnsupportedOperationException();
//        };
//    }

//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        return http.authorizeExchange().anyExchange().permitAll().and().build();
//    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().anyRequest();
                //.antMatchers("/**");

    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange((exchange) -> {
            exchange.matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            //exchange.pathMatchers("/foo", "/bar").authenticated();
            exchange.anyExchange().permitAll();
        });
        //http.formLogin(withDefaults());
        return http.build();
    }

}
