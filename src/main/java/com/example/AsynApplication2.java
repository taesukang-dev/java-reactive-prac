package com.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

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

    public static class AcceptCompletion<S> extends Completion<S, Void> {
        Consumer<S> con;

        public AcceptCompletion(Consumer<S> con) {
            this.con = con;
        }

        @Override
        void run(S value) {
            con.accept(value);
        }
    }

    public static class ErrorCompletion<T> extends Completion<T, T> {
        Consumer<Throwable> econ;
        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        void run(T value) {
            if (next != null) next.run(value);
        }

        @Override
        void error(Throwable e) {
            econ.accept(e);
        }
    }

    public static class ApplyCompletion<S, T> extends Completion<S, T> {
        Function<S, ListenableFuture<T>> fn;
        public ApplyCompletion(Function<S, ListenableFuture<T>> fn) {
            this.fn = fn;
        }

        @Override
        void run(S value) {
            ListenableFuture<T> lf = fn.apply(value);
            lf.addCallback(s -> complete(s), e -> error(e));
        }
    }


    // Custom Mono ?
    public static class Completion<S, T> {
        Completion next;

        // accept 하고 끝
        public void andAccept(Consumer<T> con) {
            Completion<T, Void> c = new AcceptCompletion<>(con);
            this.next = c;
        }

        public Completion<T, T> andError(Consumer<Throwable> econ) {
            Completion<T, T> c = new ErrorCompletion<>(econ);
            this.next = c;
            return c;
        }

        public <V> Completion<T, V> andApply(Function<T, ListenableFuture<V>> fn) {
            Completion<T, V> c = new ApplyCompletion<>(fn);
            this.next = c;
            return c;
        }

        public static <S, T> Completion from(ListenableFuture<T> lf) {
            Completion<S, T> c = new Completion<>();
            lf.addCallback(s -> {
                c.complete(s);
            }, e-> {
                c.error(e);
            });
            return c;
        }

        void error(Throwable throwable) {
            if (next != null) next.error(throwable);
        }

        void complete(T s) {
            if (next != null) next.run(s);
        }

        void run(S value) {

        }

    }

    public static void main(String[] args) {
        SpringApplication.run(AsynApplication2.class, args);
    }
}
