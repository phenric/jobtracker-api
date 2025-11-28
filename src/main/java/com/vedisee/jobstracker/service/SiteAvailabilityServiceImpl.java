package com.vedisee.jobstracker.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteAvailabilityServiceImpl implements SiteAvailabilityService {

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public boolean isSiteReachable(String site) {
        try {
            webClient.head()
                    .uri(site)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("{} is reachable", site);
            return true;
        } catch (WebClientResponseException e) {
            log.error("{} returns status {}", site, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("{} is not reachable because of {}", site, e.getMessage());
            return false;
        }
    }
}
