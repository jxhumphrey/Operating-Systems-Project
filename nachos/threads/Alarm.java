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
        System.out.println("6");
//        if(waitQueue.peek() != null && waitQueue.peek().getWakeTime() > Machine.timer().getTime())
//            waitQueue.remove().getThread().ready();
        System.out.println("7");
        Machine.interrupt().enable();
	KThread.currentThread().yield();
        System.out.println("8");
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
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
//	long wakeTime = Machine.timer().getTime() + x;
//	while (wakeTime > Machine.timer().getTime())
//	    KThread.yield();
        System.out.println("1");
        Machine.interrupt().disable();
        System.out.println("2");
        long wakeTime = Machine.timer().getTime() + x;
        System.out.println("3");
        SleepingThread sleepingThread = new SleepingThread(KThread.currentThread(), wakeTime);
        waitQueue.add(sleepingThread);
        System.out.println("4");
        System.out.println("5");
        KThread.sleep();
        Machine.interrupt().enable();
    }
    
    public static void selfTest() {
        System.out.println("--------------Testing Alarm------------------");
        final Alarm TestAlarm = new Alarm();
        System.out.println("Creating test thread..."); 
        KThread TestThread = new KThread();
        TestThread.setTarget(new Runnable(){
            public void run(){
                System.out.println("Sleeping test thread (" + TestThread.getName() + ") for 100 ticks until (" +  (Machine.timer().getTime() + 100)
                        + ")\n\tCurrent time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(100);
                System.out.println("0");
                TestAlarm.timerInterrupt();
                System.out.println("\tCurrent time: " + Machine.timer().getTime());
                
            }
        });
        TestThread.fork();
        TestThread.join();
        
        System.out.println("-----------Testing Alarm Complete------------\n");
    }
}