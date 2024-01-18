import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FluxScEx {
    public static void main(String[] args) throws InterruptedException {
//        Flux.range(1, 10)
//                .publishOn(Schedulers.newSingle("pub"))
//                .log()
//                .subscribeOn(Schedulers.newSingle("sub"))
//                .subscribe(System.out::println);
//        System.out.println("exit");

        // user thread, daemon thread
        // jvm 은 user thread 가 하나도 없으면 종료한다.
        // daemon thread 는 user thread 가 하나도 없으면 종료된다.
        // flux.interval 은 daemon thread 이기 때문에 main thread 가 종료되면 같이 종료된다.
        Flux.interval(Duration.ofMillis(200))
                .take(10) // 10 개만 받고 종료
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(10);

    }
}
