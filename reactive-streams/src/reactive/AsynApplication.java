package reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.*;

// 1     ServletThread1 - req - Worker Thread(blocking IO (DB,API)) - res(html)
// 2 NIO ST2
// 3     ST3
// 4     ST4


@Slf4j
@EnableAsync
@SpringBootApplication
public class AsynApplication {

    @RestController
    public static class MyController {
        @Autowired
        MyService myService;

        @GetMapping("/callable")
        // spring 에서 callable 을 return 하면 servlet thread 를 붙잡지 않고,
        // callable 을 실행시키는 worker thread (spring.task.execution.pool.core.size) 를 만들어서 실행시킴
        public Callable<String> async() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Executors.newSingleThreadExecutor().submit(() -> {
                for (int i = 1; i <= 50; i++) {
                    try {
                        emitter.send("<p>Stream " + i + "</p>");
                        Thread.sleep(2000);
                    } catch (Exception e) { }
                }
            });
            return emitter;
        }

    }

    @Component
    public static class MyService {
//        @Async(value = "tp")
        // async 는 simpletaskexecutor 를 사용하고, request 가 들어올 때마다 1개씩 만들고, 완료되면 버려짐
        // 따라서 resource 측면에서 권장되지 않고 아래처럼 ThreadPoolTaskExecutor 를 bean 으로 등록해서 사용
        // 만약 ThreadPoolTaskExecutor 가 bean 으로 등록되어 있으면 그것을 사용하고, 없으면 simpletaskexecutor 를 사용
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            Thread.sleep(2000);
            log.info("hello()");
            return new AsyncResult<>("Hello");
        }
    }

//    @Bean
//    ThreadPoolTaskExecutor tp() {
//        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
//        te.setCorePoolSize(10);
//        te.setMaxPoolSize(100);
//        te.setQueueCapacity(200);
//        te.setThreadNamePrefix("mythread");
//        te.initialize();
//        return te;
//    }

    public static void main(String[] args) {
        SpringApplication.run(AsynApplication.class, args);
    }

//    @Autowired MyService myService;
//
//    @Bean
//    ApplicationRunner run() {
//        return args -> {
//            log.info("run()");
//            Future<String> f = myService.hello();
////            ListenableFuture<String> f = myService.hello();
//            log.info("exit: " +f.isDone());
//            log.info("result: " + f.get());
////            f.addCallback(s -> System.out.println(s), e -> System.out.println("error " + e.getMessage()));
//            log.info("exit");
//        };
//    }
}
