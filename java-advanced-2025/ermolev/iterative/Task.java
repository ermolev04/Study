package info.kgeorgiy.ja.ermolev.iterative;

public class Task {
    Runnable r;
    boolean Ok = false;

    public Task(Runnable r) {
        this.r = r;
    }

    public synchronized void isDone() throws InterruptedException {
        while(!Ok) {
            wait();
        }
    }

    public synchronized void run() throws InterruptedException {
        r.run();
        Ok = true;
        notify();
    }
}
