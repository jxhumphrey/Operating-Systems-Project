package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

public class ReactWater {
    
    private Lock lock;
    private LinkedList<Condition2> hydrogen;
    private LinkedList<Condition2> oxygen;
    
    public ReactWater(){
        this.lock = new Lock();
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
}
