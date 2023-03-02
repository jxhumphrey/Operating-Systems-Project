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
    private LinkedList<thread> listeners;
    private LinkedList<thread> speakers;
    
    public Communicator() {
        this.lock = new Lock();
        this.listeners = new LinkedList<>();
        this.speakers = new LinkedList<>();
    }
    
    private class thread {
        private Condition2 cond;
        private int word;
        
        public thread(Condition2 cond){
            this.cond = cond;
        }
        
        public int getWord(){
            return this.word;
        }
        
        public void setWord(int word){
            this.word = word;
        }
        
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
        if (listeners.isEmpty()){
            Condition2 cond = new Condition2(lock);
            speakers.addFirst(new thread(cond));
            speakers.getFirst().setWord(word);
            speakers.getFirst().getCondition().sleep();
            
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
        int word;
        if (speakers.isEmpty()){
            Condition2 cond = new Condition2(lock);
            listeners.addFirst(new thread(cond));
            listeners.getFirst().getCondition().sleep();
            
        } else {
            listeners.getFirst().setWord(speakers.getFirst().getWord());
            speakers.getFirst().getCondition().wake();
        }
        
        word = listeners.getFirst().getWord();
        listeners.removeFirst();
        lock.release();
        return word;
    }
}
