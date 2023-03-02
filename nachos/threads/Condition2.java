package nachos.threads;
import java.util.LinkedList;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
        /**
         * Allocate a new condition variable.
         *
         * @param	conditionLock	the lock associated with this condition
         *				variable. The current thread must hold this
        *				lock whenever it uses <tt>sleep()</tt>,
        *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
        */
        public Condition2(Lock conditionLock) {

        this.conditionLock = conditionLock;

        waitQueue = new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        
	    Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean startingStatus = Machine.interrupt().disable();

	    conditionLock.release();

        waitQueue.add(KThread.currentThread());

        KThread.sleep();

	    conditionLock.acquire();

        Machine.interrupt().restore(startingStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     * 
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean startingStatus = Machine.interrupt().disable();

    
    if (waitQueue.size()!=0){

		(waitQueue.removeFirst()).ready();
	}
	
	Machine.interrupt().restore(startingStatus);


    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {

	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        
        while (waitQueue.size()!=0){
			wake();
	    }
    }


    
    public static void selfTest(){ 

        System.out.println("\n------------Testing Condition2 Start ----------------");
        System.out.println("Three tests will be performed one for each of the methods in condtion2");
        System.out.println("These methods are: sleep(), wake() and wakeAll()\n");


        final Lock lock = new Lock();
        final Condition2 condtion2Test = new Condition2(lock);
        
        // Thread for testing sleep()
        KThread sleepTestThread = new KThread(new Runnable(){
            
            public void run(){
            
                lock.acquire();
                
                System.out.println("sleep() test: Test starting"); 
                System.out.println("sleep() test: sleep() is being called and thread is going to sleep\n");
                
                condtion2Test.sleep();
                System.out.println("sleep() test: Test was successful, thread has been woken up.\n");
                
                lock.release();
            }
      
        });
   
        sleepTestThread.fork();
        
        // Thread for testing wake()
        KThread wakeTestThread = new KThread(new Runnable(){

            public void run(){
                
                lock.acquire();
                
                System.out.println("wake() test: Test starting"); 
                System.out.println("wake() test: wake() is being called and thread from sleep() test is being woken up");
                
                condtion2Test.wake();      
                
                System.out.println("wake() test: Test was successful, thread from sleep() test has been woken up.\n");
                
                lock.release();

            }
        });
     
        wakeTestThread.fork();
       
        sleepTestThread.join();
      
        System.out.println("\nwakeAll() test: Test starting\n");
        
        // First thread for testing wakeAll()
        KThread testThread1 = new KThread(new Runnable(){

            public void run(){

                lock.acquire();

                System.out.println("wakeAll() test: sleep() is being called and Thread 1 is going to sleep");
                
                condtion2Test.sleep();      
                
                System.out.println("wakeAll() test: Thread 1 has been woken up");
                
                lock.release();
            }
        });
       
        testThread1.fork();
        
        // Second thread for testing wakeAll
        KThread testThread2 = new KThread(new Runnable(){

            public void run(){

                lock.acquire();

                System.out.println("wakeAll() test: sleep() is being called and Thread 2 is going to sleep\n");
                
                condtion2Test.sleep();      
               
                System.out.println("wakeAll() test: Thread 2 has been woken up"); 
                System.out.println("wakeAll() test: Test was successful, Thread 1 and Thread 2 are awake");
                
                lock.release();

            }
        });

        testThread2.fork();
        
        // Thread calls wakeAll()
        KThread wakeAllTestThread = new KThread(new Runnable(){

            public void run(){

                lock.acquire();

                System.out.println("wakeAll() test: wakeAll() is called");  

                condtion2Test.wakeAll();    

                lock.release();
            }
        });
     
       wakeAllTestThread.fork();
       
       wakeAllTestThread.join();

       System.out.println("------------Testing Condition2 End ----------------\n");
   }



    private Lock conditionLock;
	private LinkedList<KThread> waitQueue;

}