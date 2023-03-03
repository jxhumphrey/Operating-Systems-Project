package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    private PriorityQueue<SleepingThread> waitQueue = new PriorityQueue<>();
    
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        Machine.interrupt().disable();
        while(!waitQueue.isEmpty() && waitQueue.peek().getWakeTime() <= Machine.timer().getTime())
            waitQueue.remove().getThread().ready();
        Machine.interrupt().enable();
	KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	waitTime	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long waitTime) {
	// for now, cheat just to get something working (busy waiting is bad)
//	long wakeTime = Machine.timer().getTime() + x;
//	while (wakeTime > Machine.timer().getTime())
//	    KThread.yield();
        Machine.interrupt().disable();
        long wakeTime = Machine.timer().getTime() + waitTime;
        SleepingThread sleepingThread = new SleepingThread(KThread.currentThread(), wakeTime);
        waitQueue.add(sleepingThread);
        KThread.sleep();
        Machine.interrupt().enable();
    }
    
    private class SleepingThread implements Comparable<SleepingThread> {

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
    
    public static void selfTest() {
        System.out.println("--------------Testing Alarm------------------");
        final Alarm TestAlarm = new Alarm();
        System.out.println("Creating test thread..."); 
        KThread TestThread = new KThread();
        TestThread.setName("Test Thread 1");
        TestThread.setTarget(new Runnable(){
            public void run(){
                long waitTime = 1;
                System.out.println("Sleeping [" + TestThread.getName() + "]for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing at current time: " + Machine.timer().getTime());
                
                 waitTime = 2000;
                System.out.println("Sleeping [" + TestThread.getName() + "]for " + waitTime + " ticks until at least [" + (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime());
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing at current time: " + Machine.timer().getTime());
                
            }
        });
        TestThread.fork();
        TestThread.join();
        
        System.out.println("-----------Testing Alarm Complete------------\n");
    }
}