import java.util.ArrayList;
import java.util.List;

public class MyObserverPattern {

    public static void main(String[] args) {
        MyObservable myObservable = new MyObservable();
        MyObserver myObserver = new MyObserver();
        myObservable.addObserver(myObserver);

        myObservable.notifyObservers("Hello");
        myObservable.notifyObservers(1);
    }
}

// 이를테면 Subscriber
class MyObserver {
    // execute
    public void update(MyObservable o,  Object arg) {
        System.out.println("update " + arg);
    }
}

// 이를테면 Publisher
class MyObservable {
    List<MyObserver> observers = new ArrayList<>();

    public void addObserver(MyObserver o) {
        observers.add(o);
    }

    // trigger
    public void notifyObservers(Object arg) {
        for (MyObserver o : observers) {
            o.update(this, arg);
        }
    }
}