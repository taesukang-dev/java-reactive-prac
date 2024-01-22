package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@Slf4j
public class MonoApplication {

    @GetMapping("")
    // spring 이 controller 는 return type 이 Mono 이면 subscribe 를 해서 실행시킴
    // Mono / Flux 하나 이상의 subscribe 가 있을 수 있음
    public Mono<String> hello() {
        log.info("pos1");
        Mono<String> m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c)).log();// Publisher -> Publisher -> Subscriber
        // publishing 하는 source 하는 (hot / cold) type 이 존재
        // hot type 은 이미 데이터가 존재하는 source -> subscribe 하지 않아도 데이터가 생성됨
        // cold type 은 데이터가 없는 source -> subscribe 가 되어야지 데이터가 생성됨

        // block 은 내부에서 subscribe 를 하고, 데이터가 생성될 때까지 기다림
        // 데이터를 가져올 때까지 blocking 이 되어버림
        String block = m.block();

        log.info("pos2");
//        return m;

        return Mono.just(block);
    }

    private String generateHello() {
        log.info("method generateHello");
        return "Hello Mono";
    }

    public static void main(String[] args) {
        SpringApplication.run(MonoApplication.class, args);
    }
}
