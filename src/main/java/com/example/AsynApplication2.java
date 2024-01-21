package com.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 비동기 RestTemplate 과 비동기 MVC 결합
@SpringBootApplication
public class AsynApplication2 {

    @RestController
    public static class MyController {
        @GetMapping("/rest")
        public String rest() {
            return "rest";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AsynApplication2.class, args);
    }
}
