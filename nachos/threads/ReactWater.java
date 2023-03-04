package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * The ReactWater</i> class takes hydrogen and oxygen calls and creates
 * water using two hydrogen threads and one oxygen thread.
 */

public class ReactWater {
    
    /**
     * This class uses a lock for synchronization. It contains two queues, one for
     * hydrogen threads and another for oxygen threads.
     */
    private Lock lock;
    private LinkedList<Condition2> hydrogen;
    private LinkedList<Condition2> oxygen;
    
    /**
     * Constructor for the ReactWater initializes a new lock and two queues.
     */
    public ReactWater(){
        this.lock = new Lock();
        hydrogen = new LinkedList<>();
        oxygen = new LinkedList<>();
    }
    
    /**
     * The hReady function adds an instance of hydrogen for the water reaction.
     */
    public void hReady(){
        lock.acquire();
        System.out.println("Adding one hydrogen");
        
        // If oxygen or hydrogen queue are empty, add to hydrogen queue and wait
        if (oxygen.isEmpty() || hydrogen.isEmpty()){
            Condition2 cond = new Condition2(lock);
            hydrogen.add(cond);
            cond.sleep();
        
        // If oxygen and hydrogen contains at least one instance
        // Remove one instance of each queue and call MakeWater.
        } else {
            oxygen.getFirst().wake();
            oxygen.removeFirst();
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            this.MakeWater();
        }
        lock.release();
    }
    
    /**
     * The oReady function adds an instance of oxygen for the water reaction.
     */
    public void oReady(){
        lock.acquire();
        System.out.println("Adding one oxygen");
        
        // If hydrogen queue has less than two threads, add oxygen to queue and wait.
        if (hydrogen.size() < 2){
            Condition2 cond = new Condition2(lock);
            oxygen.add(cond);
            cond.sleep();
        
        // If hydrogen queue has more than 2 instances in the queue
        // Remove two hydrogen threads and call MakeWater.
        } else{
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            this.MakeWater();
        }
        lock.release();
    }
    
    /**
     * The MakeWater methodd simply prints out "Water was made!" when 
     * There are two hydrogen threads and one oxygen thread available.
     */
    private void MakeWater(){
        System.out.println("Water was made!!");
    }


    /**
     * This selfTest method is a test to make sure the ReactWater class is
     * able to make water with many calls of oReady and hReady
     */
    public static void selfTest(){
        System.out.println("-----------------------Testing ReactWater Start----------------------------\n");

        // Create ReactWater object to use for threads
        final ReactWater reaction = new ReactWater();
        
        // First hydrogen thread that calls hReady
        KThread hydrogenTest1 = new KThread(new Runnable(){

            public void run(){
                
                // Call hReady 5 times to check if hReady can be called multiple times
                for (int i = 0; i < 5; i++){
                    reaction.hReady();
                }
            }
        });
        
        // Second hydrogen thread that calls hReady
        KThread hydrogenTest2 = new KThread(new Runnable(){

            public void run(){
                // Another 5 hReady calls
                for (int i = 0; i < 5; i++){
                    reaction.hReady();
                }
            }
        });
        
        // Oxygen thread that calls oReady
        KThread oxygenTest = new KThread(new Runnable(){

            public void run(){
                
                // Call oReady 5 times to accomodate the 10 hReady calls
                for (int i = 0; i < 5; i++){
                    reaction.oReady();
                }
            }
        });

        // Start all threads
        hydrogenTest1.fork();
        hydrogenTest2.fork();
        oxygenTest.fork();
        
        // Join all threads back to main
        try {
            hydrogenTest1.join();
            hydrogenTest2.join();
            oxygenTest.join();
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    System.out.println("-----------------------Testing ReactWater  END----------------------------\n");


    }

} 