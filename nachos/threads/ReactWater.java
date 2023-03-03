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
        System.out.println("Adding one hydrogen");
        if (oxygen.isEmpty() || hydrogen.isEmpty()){
            Condition2 cond = new Condition2(lock);
            hydrogen.add(cond);
            cond.sleep();
        
        } else {
            oxygen.getFirst().wake();
            oxygen.removeFirst();
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            this.MakeWater();
        }
        lock.release();
    }
    
    public void oReady(){
        lock.acquire();
        System.out.println("Adding one oxygen");
        if (hydrogen.size() < 2){
            Condition2 cond = new Condition2(lock);
            oxygen.add(cond);
            cond.sleep();
        
        } else{
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            hydrogen.getFirst().wake();
            hydrogen.removeFirst();
            this.MakeWater();
        }
        lock.release();
    }
    
    private void MakeWater(){
        System.out.println("Water was made!!");
    }


    public static void selfTest(){
        System.out.println("-----------------------Testing ReactWater Start----------------------------\n");

        final ReactWater reaction = new ReactWater();
        KThread hydrogenTest1 = new KThread(new Runnable(){

            public void run(){
                
                for (int i = 0; i < 5; i++){
                    reaction.hReady();
                }
            }
        });
        
        KThread hydrogenTest2 = new KThread(new Runnable(){

            public void run(){
                
                for (int i = 0; i < 5; i++){
                    reaction.hReady();
                }
            }
        });
        
        KThread oxygenTest = new KThread(new Runnable(){

            public void run(){
                
                for (int i = 0; i < 5; i++){
                    reaction.oReady();
                }
            }
        });

        hydrogenTest1.fork();
        hydrogenTest2.fork();
        oxygenTest.fork();
        try {
            hydrogenTest1.join();
            hydrogenTest2.join();
            oxygenTest.join();
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    System.out.println("-----------------------Testing ReactWater  END----------------------------");


    }

} 