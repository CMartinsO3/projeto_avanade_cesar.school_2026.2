package com.caiooliveira.petcheckin.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O "relógio" do sistema. As regras pedem a data de hoje a ele (LocalDate.now(clock)),
 * e não direto ao computador. Assim, nos testes, dá para usar um relógio parado numa data fixa.
 */
@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
