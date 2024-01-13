import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObserverPrac {
    // Duality란? 두 개의 개념이 서로에게 반대되는 관계에 있음을 의미한다.
    // Iterable <> Observable
    // Pull <> Push

    // Reactive Streams - 표준 - Java 9 Flow API

    /** Iterable
     // list 는 iterable 이다.
     List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
     for (Integer i : list) { // for each
     System.out.println(i);
     }
     Iterable<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5);
     for (Integer i : list1) { // for each
     System.out.println(i);
     }

     Iterable<Integer> iterable = () ->
     new Iterator<Integer>() {
     int i = 0;
     final static int MAX = 10;

     public boolean hasNext() {
     return i < MAX;
     }

     public Integer next() {
     return ++i;
     }
     };

     for (Integer i : iterable) {
     System.out.println(i);
     }
     * @param args
     */

    static class InteObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                setChanged();
                notifyObservers(i); // push
                // int i = it.next(); // pull
            }
        }
    }

    // DATA method(void) <-> void method(DATA)

    public static void main(String[] args) {
        // Observable
        // source -> event/data -> Observer

        Observer ob = new java.util.Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        InteObservable io = new InteObservable();
        io.addObserver(ob);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        executorService.shutdown();

        // Oservable 의 단점
        // 1. Complete? 라는 개념이 없다.
        // 2. Error 처리가 잘 되어 있지 않다.
     }
}
