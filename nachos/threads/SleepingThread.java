package nachos.threads;

public class SleepingThread implements Comparable<SleepingThread> {
    KThread thread;
    long wakeTime;

    public SleepingThread(KThread thread, long wakeTime) {
        this.thread = thread;
        this.wakeTime = wakeTime;
    }

    public KThread getThread() {
        return thread;
    }

    public long getWakeTime() {
        return wakeTime;
    }

    @Override
    public int compareTo(SleepingThread o) {
        long x = this.getWakeTime();
        long y = o.getWakeTime();
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}