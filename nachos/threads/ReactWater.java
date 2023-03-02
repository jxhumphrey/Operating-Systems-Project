package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

public class ReactWater {
    
    private Lock lock;
    private LinkedList<Condition2> hydrogen;
    private LinkedList<Condition2> oxygen;
    
    public ReactWater(Lock lock){
        this.lock = lock;
        hydrogen = new LinkedList<>();
        oxygen = new LinkedList<>();
    }
    
    public void hReady(){
        lock.acquire();
        if (oxygen.isEmpty() || hydrogen.isEmpty()){
            hydrogen.addFirst(new Condition2(lock));
            hydrogen.getFirst().sleep();
        
        } else {
            oxygen.getFirst().wake();
            hydrogen.getFirst().wake();
            this.MakeWater();
        }
        lock.release();
    }
    
    public void oReady(){
        lock.acquire();
        if (hydrogen.size() < 2){
            oxygen.addFirst(new Condition2(lock));
            oxygen.getFirst().sleep();
        
        } else{
            hydrogen.getFirst().wake();
            hydrogen.getFirst().wake();
            this.MakeWater();
        }
        lock.release();
    }
    
    private void MakeWater(){
        lock.acquire();
        oxygen.removeFirst();
        hydrogen.removeFirst();
        hydrogen.removeFirst();
        System.out.println("Water was made!!");
        lock.release();
    }
    
    public static void selfTest(){
        System.out.println("--------------Testing ReactWater------------------");
        
        final Lock lock = new Lock();
		final ReactWater react = new ReactWater(lock);

		KThread water1 = new KThread(new Runnable(){

            public void run(){
                lock.acquire();
                System.out.println("Test Case 1: 1 Hydrogen Atom");
                react.hReady();    
                //System.out.println("Test Case 1: Water shouldn't have been made because we only have " + react.hydroCount + " hydrogen atoms and " + react.oxyCount + " oxygen atoms."); 
				System.out.println("Test Case 1: Complete");
				lock.release();
				
        } } ).setName("Test 1");
		water1.fork();
        water1.join();

        System.out.println("-----------Testing ReactWater Complete------------");
    }
}
