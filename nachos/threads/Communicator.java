package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {

    private Lock lock;
    private LinkedList<Thread> listeners;
    private LinkedList<Thread> speakers;

    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        
        /**
         * Create a single lock to control synchronization and two queues
         * for the listener threads and the speaker threads.
         */
        this.lock = new Lock();
        this.listeners = new LinkedList<>();
        this.speakers = new LinkedList<>();
    }
    
    /**
     * This private class makes it possible to store the threads with
     * their condition variables and the word to be transferred.
     */
    private class Thread {
        
        /**
         * Using a new condition2 variable for each thread
         * 
         * The word is set by the speaker thread, which then sets the
         * listener thread's word
         */
        private Condition2 cond;
        private int word;
        
        /**
         * Constructor for the thread class
         * Creates a new condition2 variable for the thread
         */
        public Thread(){
            this.cond = new Condition2(lock);
        }
        
        /**
         * 
         * @return the word in the thread
         */
        public int getWord(){
            return this.word;
        }
        
        /**
         * 
         * @param word the integer that will be passed from the speaker
         * to the listener
         * Sets the word for the thread
         */
        public void setWord(int word){
            this.word = word;
        }
        
        /**
         * 
         * @return the condition2 variable of the thread
         */
        public Condition2 getCondition(){
            return cond;
        }
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        System.out.println("Speaker call");
        
        // If the listener queue is empty, add the thread with its word into the queue
        if (listeners.isEmpty()){
            Thread speakerThread = new Thread();
            speakerThread.setWord(word);
            speakers.add(speakerThread);
            speakerThread.getCondition().sleep();   //wait for a listener call
        
        // If the listener queue is not empty, get the first listener thread
        } else {
            listeners.getFirst().setWord(word);             //transfer the word
            listeners.getFirst().getCondition().wake();     //wake the listener
            listeners.removeFirst();                        //remove from queue
        }
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();
        System.out.println("Listener call");
        int word;
        
        // If the speaker queue is empty, add listener to queue and wait
        if (speakers.isEmpty()){
            Thread listenerThread = new Thread();
            listeners.add(listenerThread);
            listenerThread.getCondition().sleep();
            word = listenerThread.getWord();            //Get word that was set by speaker
        
        // If the speaker queue is not empty, get word from first speaker and remove rom queue
        } else {
            speakers.getFirst().getCondition().wake();
            word = speakers.getFirst().getWord();
            speakers.removeFirst();
        }
        
        lock.release();
        return word;
    }
    
    /**
     * This method tests that the communicator can establish communication
     * between two threads
     */
    public static void selfTest(){
        
        Communicator com = new Communicator();
        
        System.out.println("\n------------------Attempting test on communicator class------------------\n");
        
        /**
         * speakTeset calls the speak method  times
         */
        KThread speakTest = new KThread(new Runnable(){
            
            public void run(){
                for (int i = 0; i < 7; i++){
                    com.speak(i);
                }
            }
      
        });
        
        /**
         * listenTeset calls the listen method  times and prints the message.
         */
        KThread listenTest = new KThread(new Runnable(){
            
            public void run(){
                for (int i = 0; i < 7; i++){
                    System.out.println("Message recieved: " + com.listen());
                }
            }
      
        });
        
        //Start both threads
        speakTest.fork();
        listenTest.fork();
        
        //Join both threads
        try {
            speakTest.join();
            listenTest.join();
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        System.out.println("------------------Communicator class successfully passed------------------\n");
    }
}
