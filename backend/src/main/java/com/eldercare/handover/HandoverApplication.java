package com.eldercare.handover;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HandoverApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandoverApplication.class, args);
    }

    //Injected everywhere instead of calling now() directly, so time can be controlled in tests.
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
