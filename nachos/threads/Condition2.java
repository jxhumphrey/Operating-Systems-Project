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

        //Variables for testing functions
        final Lock lock = new Lock();
        final Condition2 con2 = new Condition2(lock);
      
        KThread sleep = new KThread(new Runnable(){
            
            //Test 1: Sleep
            public void run(){
            
            //get the Lock
            lock.acquire();
            
            System.out.println("sleep() test: Test starting"); 
            System.out.println("sleep() test: sleep() is being called and thread is going to sleep\n");
            
            con2.sleep();
            System.out.println("sleep() test: Test was successful, thread has been woken up.\n");
            
            lock.release();
            }
      
        });
   
        sleep.fork();
      
        KThread wake = new KThread(new Runnable()
        {
            //Test 2: Wake
            public void run(){
                
                lock.acquire();
                System.out.println("wake() test: Test starting"); 
                System.out.println("wake() test: wake() is being called and thread from sleep() test is being woken up");
                con2.wake();      
                System.out.println("wake() test: Test was successful, thread from sleep() test has been woken up.\n");
                lock.release();

            } } );
     
        wake.fork();
       
        sleep.join();
      
        System.out.println("\nwakeAll() test: Test starting\n");
       
        KThread sleep1 = new KThread(new Runnable(){
            //Test 3: Wake All sleeping thread 1
            public void run(){

                lock.acquire();
                System.out.println("wakeAll() test: Thread 1 going to sleep");
                con2.sleep();      
                System.out.println("wakeAll() test: Thread 1 has been woken up");
                lock.release();
            } } );
       
        sleep1.fork();
       
        KThread sleep2 = new KThread(new Runnable(){
            //Test 3: Wake All sleeping thead 2
            public void run(){

                lock.acquire();
                System.out.println("wakeAll() test: Thread 2 going to sleep\n");
                con2.sleep();      
                System.out.println("wakeAll() test: Thread 2 has been woken up");

                System.out.println("wakeAll() test: Test was successful, Thread 1 and Thread 2 are awake");
                lock.release();

            } } );

       sleep2.fork();
       
        KThread wakeall = new KThread(new Runnable(){
            //Test 3: Wake all

            public void run(){
                lock.acquire();
                System.out.println("wakeAll() test: wakeAll() is called");  

                con2.wakeAll();    
                lock.release();
            } } );
     
       wakeall.fork();
       
       wakeall.join();

       System.out.println("------------Testing Condition2 End ----------------\n");
   }



    private Lock conditionLock;
	private LinkedList<KThread> waitQueue;

}