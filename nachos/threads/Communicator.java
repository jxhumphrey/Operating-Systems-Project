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
    /**
     * Allocate a new communicator.
     */
    private Lock lock;
    private LinkedList<Thread> listeners;
    private LinkedList<Thread> speakers;
    
    public Communicator() {
        this.lock = new Lock();
        this.listeners = new LinkedList<>();
        this.speakers = new LinkedList<>();
    }
    
    private class Thread {
        private Condition cond;
        private int word;
        
        public Thread(){
            this.cond = new Condition(lock);
        }
        
        public int getWord(){
            return this.word;
        }
        
        public void setWord(int word){
            this.word = word;
        }
        
        public Condition getCondition(){
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
        if (listeners.isEmpty()){
            Thread speakerThread = new Thread();
            speakerThread.setWord(word);
            speakers.add(speakerThread);
            speakerThread.getCondition().sleep();
            
        } else {
            listeners.getFirst().setWord(word);
            listeners.getFirst().getCondition().wake();
            listeners.removeFirst();
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
        if (speakers.isEmpty()){
            Thread listenerThread = new Thread();
            listeners.add(listenerThread);
            listenerThread.getCondition().sleep();
            word = listenerThread.getWord();
            
        } else {
            speakers.getFirst().getCondition().wake();
            word = speakers.getFirst().getWord();
            speakers.removeFirst();
        }
        
        lock.release();
        return word;
    }
    
    public static void selfTest(){
        
        Communicator com = new Communicator();
        
        System.out.println();
        System.out.println("------------------Attempting test on communicator class------------------");
        KThread speakTest = new KThread(new Runnable(){
            
            public void run(){
                for (int i = 0; i < 5; i++){
                    com.speak(i);
                }
            }
      
        });
        
        KThread listenTest = new KThread(new Runnable(){
            
            public void run(){
                for (int i = 0; i < 5; i++){
                    System.out.println(com.listen());
                }
            }
      
        });
        speakTest.fork();
        listenTest.fork();
        
        try {
            speakTest.join();
            listenTest.join();
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("------------------Communicator class successfully passed------------------");
        System.out.println();
    }
}
