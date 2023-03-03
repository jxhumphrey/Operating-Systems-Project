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
    
    public void waitUntilBusy(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	while (wakeTime > Machine.timer().getTime())
	    KThread.yield();
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
        System.out.println("Creating test thread, thread will wait for 0 then 2000 ticks, finish time should always be larger than start time + wait time."); 
        KThread TestThread = new KThread();
        TestThread.setName("Test Thread 0");
        TestThread.setTarget(new Runnable(){
            public void run(){
                long waitTime = 0;
                System.out.println("Sleeping [" + TestThread.getName() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing at current time: " + Machine.timer().getTime());
                
                waitTime = 2000;
                System.out.println("Sleeping [" + TestThread.getName() + "] for " + waitTime + " ticks until at least [" + (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime());
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread.getName() + "] at current time: " + Machine.timer().getTime());
            }
        });
        
        TestThread.fork();
        TestThread.join();
        
        System.out.println("\nCreating test thread, thread will wait for -100 ticks, thread should wake at any time after."); 
        KThread TestThread4 = new KThread();
        TestThread4.setName("Test Thread 4");
        TestThread4.setTarget(new Runnable(){
            public void run(){
                long waitTime = -100;
                System.out.println("Sleeping [" + TestThread4.getName() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("Finishing [" + TestThread4.getName() + "] at current time: " + Machine.timer().getTime());
            }
        });
        TestThread4.fork();
        TestThread4.join();
        
        System.out.println("\nCreating 3 test threads and sleeping them simultaneously, threads should successfully wake and in reverse order."); 
        KThread TestThread1 = new KThread();
        TestThread1.setName("Test Thread 1");
        TestThread1.setTarget(new Runnable(){
            public void run(){
                long waitTime = 3000;
                System.out.println("Sleeping [" + TestThread1.getName() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("Finishing [" + TestThread1.getName() + "] at current time: " + Machine.timer().getTime());
            }
        });
        
        KThread TestThread2 = new KThread();
        TestThread2.setName("Test Thread 2");
        TestThread2.setTarget(new Runnable(){
            public void run(){
                long waitTime = 2000;
                System.out.println("Sleeping [" + TestThread2.getName() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("Finishing [" + TestThread2.getName() + "] at current time: " + Machine.timer().getTime());
            }
        });
        
        KThread TestThread3 = new KThread();
        TestThread3.setName("Test Thread 3");
        TestThread3.setTarget(new Runnable(){
            public void run(){
                long waitTime = 1000;
                System.out.println("Sleeping [" + TestThread3.getName() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime)
                        + "]\n\tStarting at current time: " + Machine.timer().getTime()); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("Finishing [" + TestThread3.getName() + "] at current time: " + Machine.timer().getTime());
            }
        });
        
        TestThread1.fork();
        TestThread2.fork();
        TestThread3.fork();
        TestThread1.join();
        
        int numThreads = 1000;
        System.out.println("\nCreating " + numThreads + " test threads and sleeping them simultaneously, using blocking.");
        KThread[] TestThreads = new KThread[numThreads];
        long x = Machine.timer().getTime();
        for(int i=0; i<numThreads; ++i){
            TestThreads[i] = new KThread();
            TestThreads[i].setTarget(new Runnable(){
                public void run(){
                    long waitTime = 1000;
                    TestAlarm.waitUntil(waitTime);
                }
            });
        }
        for(int i=0; i<numThreads; ++i)
            TestThreads[i].fork();
        for(int i=0; i<numThreads; ++i)
            TestThreads[i].join();
        System.out.println("Time to complete: " + (Machine.timer().getTime() - x));
        
        System.out.println("\nCreating " + numThreads + " test threads and sleeping them simultaneously, using busy waiting.");
        KThread[] TestThreads2 = new KThread[numThreads];
        x = Machine.timer().getTime();
        for(int i=0; i<numThreads; ++i){
            TestThreads2[i] = new KThread();
            TestThreads2[i].setTarget(new Runnable(){
                public void run(){
                    long waitTime = 1000;
                    TestAlarm.waitUntilBusy(waitTime);
                }
            });
        }
        for(int i=0; i<numThreads; ++i)
            TestThreads2[i].fork();
        for(int i=0; i<numThreads; ++i)
            TestThreads2[i].join();
        System.out.println("Time to complete: " + (Machine.timer().getTime() - x));
        System.out.println("-----------Testing Alarm Complete------------\n");
    }
}