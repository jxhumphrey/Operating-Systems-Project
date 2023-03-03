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


	public static void selfTest()
	{
        System.out.println("-----------------------Testing ReactWater Start----------------------------\n");

		final Lock selfTestLock = new Lock();
		final ReactWater reaction = new ReactWater(selfTestLock);
		KThread waterTest1 = new KThread(new Runnable(){

            public void run(){
                selfTestLock.acquire();
                System.out.println("Test 1: 1 Hydrogen Atom and 1 Oxygen Atom");
                reaction.oReady();
                reaction.hReady(); 
                System.out.println("Test 1: Water wasn't made because there is only " + reaction.hydrogen.size() + " hydrogen atom and " + reaction.oxygen.size() + " oxygen atom."); 
				System.out.println("Test 1: Complete\n");
				selfTestLock.release();
				
        }});
		waterTest1.fork();
        waterTest1.join();
		
		KThread waterTest2 = new KThread(new Runnable(){

            public void run(){
                selfTestLock.acquire();
                System.out.println("Test 2: Add 1 hydrogen atom");
                reaction.hReady();    
                System.out.println("Test 2: Water should have been made with " + reaction.hydrogen.size() + " hydrogen atoms left over and " + reaction.oxygen.size() + " oxygen atoms left over."); 
				System.out.println("Test 2: Complete\n");
				selfTestLock.release();
			}
        });
		waterTest2.fork();
		waterTest2.join();
		
		KThread waterTest3 = new KThread(new Runnable(){

            public void run(){
                selfTestLock.acquire();
                System.out.println("Test 3: Add 4 Hydrogen Atoms");
                reaction.hReady();  
                reaction.hReady(); 
                reaction.hReady(); 
                reaction.hReady();   
                System.out.println("Test 3: Water wasn't made because there is only " + reaction.hydrogen.size() + " hydrogen atom and " + reaction.oxygen.size() + " oxygen atom."); 
				System.out.println("Test 3: Complete\n");
				selfTestLock.release();
			}
        });

		waterTest3.fork();
        waterTest3.join();
		
		
		KThread waterTest4 = new KThread(new Runnable(){

            public void run(){
                selfTestLock.acquire();
                System.out.println("Test 4: Add 2 Oxygen Atoms");
                reaction.oReady();
                reaction.oReady();    
                System.out.println("Test 4: Water should have been made twice with " + reaction.hydrogen.size() + " hydrogen atoms and " + reaction.oxygen.size() + " oxygen atoms left over."); 
				System.out.println("Test 4: Complete\n");
				selfTestLock.release();
			}
        });

		waterTest4.fork();
		waterTest4.join();

        System.out.println("-----------------------Testing ReactWater  END----------------------------");

		
	}

} 