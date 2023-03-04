package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    //Priority queue to sort and contain sleeping threads.
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
        //if waitQueue has threads ready, wake them
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
        Machine.interrupt().disable();
        long wakeTime = Machine.timer().getTime() + waitTime;
        //store KThread and associated wake time as a SleepingThread in the waitQueue then sleep it.
        SleepingThread sleepingThread = new SleepingThread(KThread.currentThread(), wakeTime);
        waitQueue.add(sleepingThread);
        KThread.sleep();
        Machine.interrupt().enable();
    }
    
    /**
     * Put the current thread to sleep for at least <i>x</i> ticks
     * using busy waiting. The thread must be woken up (placed in 
     * the scheduler ready set) during the first timer interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	waitTime	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntilBusy(long x) {
	long wakeTime = Machine.timer().getTime() + x;
	while (wakeTime > Machine.timer().getTime())
	    KThread.yield();
    }
    
    /* 
     * Encapsulates a KThread along with an associated wake time. 
     */
    private class SleepingThread implements Comparable<SleepingThread> {

        KThread thread;
        long wakeTime;

        public SleepingThread(KThread thread, long wakeTime) {
            this.thread = thread;
            this.wakeTime = wakeTime;
        }

        public KThread getThread() { return thread; }

        public long getWakeTime() { return wakeTime; }

        @Override
        public int compareTo(SleepingThread o) {
            return (this.getWakeTime() < o.getWakeTime()) ? -1 :
                    ((this.getWakeTime() == o.getWakeTime()) ? 0 : 1);
        }
    }
    
    /*
     * Run three general tests: that a thread wont wake before its wake time,
     * that multiple threads can sleep at the same time and  will wake in the correct order,
     * and that a thread with a negative wake time will wake and at any time.
     * Runs one performance test that tests that waitUntil implementation using blocking
     * is faster than that busy waiting.
     */
    public static void selfTest() {
        System.out.println("------------------Testing Alarm--------------------");
        final Alarm TestAlarm = new Alarm();
        System.out.println("TEST 1: Creating test thread, thread will wait for 0 then 2000 ticks, finish time should always be larger than start time + wait time."); 
        KThread TestThread = new KThread();
        TestThread.setName("Test Thread 0");
        TestThread.setTarget(new Runnable(){
            public void run(){
                long waitTime = 0;
                System.out.println("Sleeping [" + TestThread.getName() + "] at current time [" + Machine.timer().getTime() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread.getName() + "] at current time [" + Machine.timer().getTime() + "]");
                
                waitTime = 2000;
                System.out.println("Sleeping [" + TestThread.getName() + "] at current time [" + Machine.timer().getTime() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread.getName() + "] at current time [" + Machine.timer().getTime() + "]");
            }
        });
        
        TestThread.fork();
        TestThread.join();
        
        System.out.println("\nTEST 2: Creating 3 test threads and sleeping them simultaneously, threads should successfully wake and in reverse order."); 
        KThread TestThread1 = new KThread();
        TestThread1.setName("Test Thread 1");
        TestThread1.setTarget(new Runnable(){
            public void run(){
                long waitTime = 3000;
                System.out.println("Sleeping [" + TestThread1.getName() + "] at current time [" + Machine.timer().getTime() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread1.getName() + "] at current time [" + Machine.timer().getTime() + "]");
            }
        });
        
        KThread TestThread2 = new KThread();
        TestThread2.setName("Test Thread 2");
        TestThread2.setTarget(new Runnable(){
            public void run(){
                long waitTime = 2000;
                System.out.println("Sleeping [" + TestThread2.getName() + "] at current time [" + Machine.timer().getTime() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread2.getName() + "] at current time [" + Machine.timer().getTime() + "]");
            }
        });
        
        KThread TestThread3 = new KThread();
        TestThread3.setName("Test Thread 3");
        TestThread3.setTarget(new Runnable(){
            public void run(){
                long waitTime = 1000;
                System.out.println("Sleeping [" + TestThread3.getName() + "] at current time [" + Machine.timer().getTime() + "] for " + waitTime + " ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread3.getName() + "] at current time [" + Machine.timer().getTime() + "]");
            }
        });
        
        TestThread1.fork();
        TestThread2.fork();
        TestThread3.fork();
        TestThread1.join();
        
        
        System.out.println("\nTEST 3: Creating test thread, thread will wait for [-100] ticks, thread should wake at any time after."); 
        KThread TestThread4 = new KThread();
        TestThread4.setName("Test Thread 4");
        TestThread4.setTarget(new Runnable(){
            public void run(){
                long waitTime = -100;
                System.out.println("Sleeping [" + TestThread4.getName() + "] at current time [" + Machine.timer().getTime() + "] for [" + waitTime + "] ticks until at least [" +  (Machine.timer().getTime() + waitTime) + "]"); 
                TestAlarm.waitUntil(waitTime);
                System.out.println("\tFinishing [" + TestThread4.getName() + "] at current time [" + Machine.timer().getTime() + "]");
            }
        });
        TestThread4.fork();
        TestThread4.join();
        
        int numThreads = 1000;
        long waitTime = 1000;
        System.out.println("\nTEST 4 (performance): Creating and sleeping muiltiple threads using blocking then busy waiting, blocking should be faster.\nCreating [" + numThreads + "] test threads and sleeping them for [" + waitTime + "] ticks simultaneously using blocking.");
        KThread[] TestThreads = new KThread[numThreads];
        long x = Machine.timer().getTime();
        for(int i=0; i<numThreads; ++i){
            TestThreads[i] = new KThread();
            TestThreads[i].setTarget(new Runnable(){
                public void run(){
                    TestAlarm.waitUntil(waitTime);
                }
            });
        }
        for(int i=0; i<numThreads; ++i) TestThreads[i].fork();
        for(int i=0; i<numThreads; ++i) TestThreads[i].join();
        System.out.println("\tTime to complete [" + (Machine.timer().getTime() - x) + "]");
        
        System.out.println("Creating [" + numThreads + "] test threads and sleeping them for [" + waitTime + "] ticks simultaneously using busy waiting.");
        KThread[] TestThreads2 = new KThread[numThreads];
        x = Machine.timer().getTime();
        for(int i=0; i<numThreads; ++i){
            TestThreads2[i] = new KThread();
            TestThreads2[i].setTarget(new Runnable(){
                public void run(){
                    TestAlarm.waitUntilBusy(waitTime);
                }
            });
        }
        for(int i=0; i<numThreads; ++i) TestThreads2[i].fork();
        for(int i=0; i<numThreads; ++i) TestThreads2[i].join();
        System.out.println("\tTime to complete [" + (Machine.timer().getTime() - x) + "]");
        System.out.println("\nAll tests pass.");
        System.out.println("--------------Testing Alarm Complete---------------\n");
    }
}