package com.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// 비동기 RestTemplate 과 비동기 MVC 결합
@SpringBootApplication
public class AsynApplication2 {

    @RestController
    public static class MyController {
        RestTemplate rt = new RestTemplate();
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();

        @GetMapping("/rest")
        // webclient 는 reactor 를사용
        // visualvm 확인 결과 실제로 100개 를 req 하더라도 reactor http nio 는 8개 정도 사용
        // reactor 는 core 의 1배수로 thread 를 사용
        public Mono<String> rest(int idx) {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/service")
                            .queryParam("req", "hello" + idx)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AsynApplication2.class, args);
    }
}
