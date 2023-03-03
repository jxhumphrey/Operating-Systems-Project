package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
public class ReactWater{

    
	private LinkedList<KThread> hydrogen;
	private LinkedList<KThread> oxygen;
	private Lock lock = new Lock();
	
    public ReactWater(Lock lock){

		this.lock = lock;
        hydrogen = new LinkedList<KThread>();
        oxygen = new LinkedList<KThread>();

    } 

    public void hReady() {
		
		hydrogen.add(KThread.currentThread());
        makeWater();

    }
 
    public void oReady() {
		
		oxygen.add(KThread.currentThread());
        makeWater();

    } 
    
    public void makeWater(){

		Lib.assertTrue(lock.isHeldByCurrentThread());	

		while(oxygen.size() >= 1 &&  hydrogen.size() >= 2)
		{
			System.out.println("Water was made!!");
			hydrogen.removeFirst();
			hydrogen.removeFirst();
			oxygen.removeFirst();

		}

    } 
	public static void selfTest()
	{
        System.out.println("-----------------------ReactWater Testing Start----------------------------");

		final Lock testLock = new Lock();
		final ReactWater react = new ReactWater(testLock);
		KThread waterTest1 = new KThread(new Runnable(){

            public void run(){
                testLock.acquire();
                System.out.println("Test Case 1: 1 Hydrogen Atom and 1 Oxygen Atom");
                react.hReady(); 
                react.oReady();
                 
                System.out.println("Test Case 1: Water shouldn't have been made because we only have " + react.hydrogen.size() + " hydrogen atoms and " + react.oxygen.size() + " oxygen atoms."); 
				System.out.println("Test Case 1: Complete");
				testLock.release();
				
        }});
		waterTest1.fork();
        waterTest1.join();
		
		KThread waterTest2 = new KThread(new Runnable(){

            public void run(){
                testLock.acquire();
                System.out.println("Test Case 2: 2 Hydrogen Atoms");
                react.hReady();    
                System.out.println("Test Case 2: Water shouldn't have been made because we only have " + react.hydrogen.size() + " hydrogen atoms and " + react.oxygen.size() + " oxygen atoms."); 
				System.out.println("Test Case 2: Complete");
				testLock.release();
			}
        });
		waterTest2.fork();
		waterTest2.join();
		
		KThread waterTest3 = new KThread(new Runnable(){

            public void run(){
                testLock.acquire();
                System.out.println("Test Case 3: 3 Hydrogen Atoms");
                react.hReady();    
                System.out.println("Test Case 3: Water shouldn't have been made because we only have " + react.hydrogen.size() + " hydrogen atoms and " + react.oxygen.size() + " oxygen atoms."); 
				System.out.println("Test Case 3: Complete");
				testLock.release();
			}
        });

		waterTest3.fork();
        waterTest3.join();
		
		
		KThread waterTest4 = new KThread(new Runnable(){

            public void run(){
                testLock.acquire();
                System.out.println("Test Case 4: 3 Hydrogen Atoms, 1 Oxygen Atom");
                react.oReady();    
                System.out.println("Test Case 4: Water should have been made with " + react.hydrogen.size() + " hydrogen atoms and " + react.oxygen.size() + " oxygen atoms left over."); 
				System.out.println("Test Case 4: Complete");
				testLock.release();
			}
        });
        
		waterTest4.fork();
		waterTest4.join();
        System.out.println("-----------------------ReactWater Testing END----------------------------");

		
	}

} // end of class ReactWater