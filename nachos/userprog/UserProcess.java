package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
        //TASK 2.1
        processLock = new Lock();
        fileList = new OpenFile[MAX_FILES];
        filesToBeDeleted = new HashSet<String>();
        fileList[INPUT] = UserKernel.console.openForReading();
        fileList[OUTPUT] = UserKernel.console.openForWriting();
        //TASK 2.1
        
	int numPhysPages = Machine.processor().getNumPhysPages();
	pageTable = new TranslationEntry[numPhysPages];
	for (int i=0; i<numPhysPages; i++)
	    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
        
        //used in task 3, requires mutex
        ID = processCount;
        Machine.interrupt().disable();
        processLock.acquire();
        processCount++;
        processLock.release();
        Machine.interrupt().enable();
        parent = null;
    }
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	//changed for join()
	thread = (UThread)(new UThread(this).setName(name));
        thread.fork();

	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	
	// for now, just assume that virtual addresses equal physical addresses
	if (vaddr < 0 || vaddr >= memory.length)
	    return 0;

	int amount = Math.min(length, memory.length-vaddr);
	System.arraycopy(memory, vaddr, data, offset, amount);

	return amount;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	
	// for now, just assume that virtual addresses equal physical addresses
	if (vaddr < 0 || vaddr >= memory.length)
	    return 0;

	int amount = Math.min(length, memory.length-vaddr);
	System.arraycopy(data, offset, memory, vaddr, amount);

	return amount;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;

		// for now, just assume virtual addresses=physical addresses
		section.loadPage(i, vpn);
	    }
	}
	
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {
        if(ID != 0) {
            Machine.halt();
            Lib.assertNotReached("Machine.halt() did not halt machine!");
            return 0;
        } else
            return -1;
    }
    
    private int getNextFileDescriptor() {
        for(int i = 2; i < fileList.length; i++)
		if (fileList[i] == null)
			return i;
	return -1;
    }
    
    private int handleCreat(int vAddress) {
        String fileName = readVirtualMemoryString(vAddress, MAX_STRING_LENGTH);
	int fd = getNextFileDescriptor();
	if (fileName == null || fd == -1)
		return -1;
	OpenFile file = ThreadedKernel.fileSystem.open(fileName, false);
	if (file == null)
		return -1;
	file = ThreadedKernel.fileSystem.open(fileName, true);
	if (file == null)
		return -1;
	return fd;
    }
    
    private int handleOpen(int vAddress) {
        String fileName = readVirtualMemoryString(vAddress, MAX_STRING_LENGTH);
	int fd = getNextFileDescriptor();
	if (fileName == null || fd == -1)
            return -1;
	OpenFile file = UserKernel.fileSystem.open(fileName, false);
	if (file == null)
            return -1;
        return fd;
    }
    
    private int handleClose(int fd) {
        if (fd >= MAX_FILES || fd < 0 || filesToBeDeleted.contains(fileList[fd].getName()))
            return -1;
        
	filesToBeDeleted.remove(fileList[fd].getName());
        fileList[fd].close();
        fileList[fd] = null;
	filePosList[fd] = 0;
	return 0;
    }
    
    private int handleWrite(int fd, int bufferStartAddress, int count) {
        if (fileList[fd] == null || fd < 0 || fd >= MAX_FILES || count < 0)
		return -1;
	byte[] dataWrite = new byte[count];
	int numBytesWritten;
        
	int numBytesToWrite = readVirtualMemory(bufferStartAddress, dataWrite, 0, count);
	if (fd > 1) {
            numBytesWritten = fileList[fd].write(filePosList[fd], dataWrite, 0,  numBytesToWrite);
            filePosList[fd] += numBytesWritten;
        }
	else
            numBytesWritten = fileList[fd].write(dataWrite, 0,  numBytesToWrite);
	
        if ( numBytesWritten != 0 && numBytesWritten < count )
            return -1;
	else
            return numBytesWritten;

    }
    
    private int handleRead(int fd, int bufferStartAddress, int count) {
        if (fileList[fd] == null || fd < 0 || fd >= MAX_FILES || count < 0)
		return -1;
	byte[] dataRead = new byte[count];
	int numBytesRead;
	if (fd > 1) {
		numBytesRead = fileList[fd].read(filePosList[fd], dataRead, 0,  count);
                filePosList[fd] += numBytesRead;
        }
	else
		numBytesRead = fileList[fd].read(dataRead, 0, count);
	if (numBytesRead == -1)
		return -1;
	int bytesWrittenToProcess = writeVirtualMemory(bufferStartAddress, dataRead, 0, numBytesRead);
        
        if (bytesWrittenToProcess == 0)
            return -1;
        return bytesWrittenToProcess;

    }
    
    private int handleUnlink(int vAddress) {
        String fileName = readVirtualMemoryString(vAddress, MAX_STRING_LENGTH);
	if (fileName == null || filesToBeDeleted.contains(fileName))
		return -1;
	boolean isDeleted = UserKernel.fileSystem.remove(fileName);
	if (!isDeleted)
		return -1;
	filesToBeDeleted.remove(fileName);
	return 0;
    }

    private static final int
        syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
	case syscallHalt:
	    return handleHalt();
        

	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!");
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;				       
				       
	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }
    
    //TASK 2.1
    private static int MAX_STRING_LENGTH = 256;
    private static final int INPUT = 0;
    private static final int  OUTPUT = 1;
    private final int MAX_FILES = 18;
    private OpenFile[] fileList;
    private int[] filePosList;
    private int[] fileDescriptorList;
    private HashSet<String> filesToBeDeleted;
    Lock processLock;
    // TASK 2.1

    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    
    /**
    Creates and executes a user program with specified arguments in a child 
    process. The method accepts the name of the file that contains the 
    executable, an integer for the number of arguments, and the arguments. 
    It will return an integer for the child process’ ID.
    */
    private int exec(int vAddr, int argCount, int argv){
        //check for invalid arguements
        if(vAddr < 0 || argCount < 0 || argv < 0) return -1;
        //get filename virtual address
        String fileName = readVirtualMemoryString(vAddr, 256);
        //check that filename is valid
        if(fileName == null || !fileName.endsWith(".coff")) return -1;
        //create string array to store arguements
        String[] args = new String[argCount];
        //iterate through arguements and place in local array
        for(int i=0; i<argCount; ++i){
            //create byte array of size 4
            byte[] argB = new byte[4];
            //check that argument virtual address is 4 bytes
            if(readVirtualMemory(argv + i * 4, argB) != 4) return -1;
            //reorder argument virtual memory address bytes into int
            int argVAddr = Lib.bytesToInt(argB, 0);
            //if argument memory address isn’t null, place it in args
            if(readVirtualMemoryString(argv, 256) != null)
                args[i] = readVirtualMemoryString(argv, 256);
            else return -1;
        }
        //create child process to execute program
        UserProcess child = UserProcess.newUserProcess();
        if(!child.execute(fileName, args)) return -1;
        //set child parent raltionships
        child.parent = this;
        this.children.add(child);
        return child.processID;
    }
    
    //TASK 2.1
    private int processID;
    private static int MAX_STRING_LENGTH = 256;
    private static final int INPUT = 0;
    private static final int  OUTPUT = 1;
    private final int MAX_FILES = 18;
    private OpenFile[] fileList;
    private int[] filePosList;
    private int[] fileDescriptorList;
    private HashSet<String> filesToBeDeleted;
    Lock processLock;
    // TASK 2.1
    
    private int processCount = 0;
    private int ID;
    private UserProcess parent;
    private LinkedList<UserProcess> children = new LinkedList<UserProcess>();
    
    /**
    Joins two processes (parent and child). The calling process waits until a 
    specified child process completes execution before continuing its own 
    execution. It accepts two arguments, an integer for the child process ID, 
    and an integer for the exit status of that child process. It will return an 
    integer according to the child process’ exit ID.
    */
    private int join(int processID, int status){
        //check arguements are valid
        if(processID < 0 || !children.contains(processID) || status < 0) 
            return -1;
        //get child process with ID == processID
        UserProcess child = null;
        for(int i = 0; i < children.size(); ++i){
            if(children.get(i).processID == processID) {
                child = children.get(i);
                break;
            }
        }
        //check if child process is null
        if(child == null) return -1;
        //join child and destroy parent/child relationship
        child.thread.join();
        child.parent = null;
        children.remove(processID);
        //store child process exit status at status virtual memory address
        byte[] exitStatus = Lib.bytesFromInt(child.exitStatus);
        if(writeVirtualMemory(status, exitStatus) == 4)
            return 1;
        else
            return 0;
    }
    
    private UThread thread;
    private int exitStatus = 0;
    
    private int exit(int status){
        //set exitStatus, unload sections, decrement processCount
        exitStatus = status;
        unloadSections();
        --processCount;
        //set every child process parent to null and remove from children list
        for(UserProcess child : children){
            child.parent = null;
            children.remove(child);
        }
        //if last process, halt machine, otherwise, finish thread
        if(processCount == 0) Machine.halt();
        else UThread.finish();
        return 0;
    }
}
