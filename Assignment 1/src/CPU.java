import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/12/15
 */
public class CPU 
{
	private final long MAX_WORD_VALUE = Integer.MAX_VALUE; // This is the maximum for a 32 bit signed number
	private final long MIN_WORD_VALUE = Integer.MIN_VALUE; // This is the maximum for a 32 bit signed number
	
	private int opCode, arithRegisterAddress, indexRegisterAddress, ea, pc; // Some variables to hold information
	
	private boolean indirect, running; // Variable for whether or not the program is running, and if indirect addressing is on
	
	private int[] arithRegister; // Variable for the arithmetic register
	
	private String nl = System.getProperty("line.separator"); // The new line character for writing to files
	
	private MEMORY mem; // A variable for the instance of MEMORY that this class will use
	
	/**
	 * The constructor for this CPU
	 * @param mem the instance of MEMORY that this CPU will use
	 */
	CPU(MEMORY mem)
	{
		// Initializes some variables
		this.mem = mem; // The instance of MEMORY used in an instance of this class
		
		opCode = 0; // Will hold the operation code from an instruction
		arithRegisterAddress = 0; // The address in the register for doing an arithmetic operation
		indexRegisterAddress = 0; // The address in the register for doing indexing
		ea = 0; // The effective area, usually used to reference a location in memory, but also used for certain other operations
		pc = 0; // The program counter. Used to keep track of what operation the program is currently executing
		
		indirect = false; // A variable to keep track of whether or not indirect addressing is currently in use
		running = false; // A variable used to keep track of whether or not the program is currently running jobs
		
		arithRegister = new int[0xf]; // Creates a register used primarily for arithmetic operations, but also for indexing
	}

	/**
	 * Executes the next job
	 * @param firstMemoryLocation the location of the first instruction to execute. Should be passed in through LOADER.laodNextJob()
	 * @throws IOException
	 */
	public void execute(int firstMemoryLocation) throws IOException 
	{
		// Running starts out true and we set the program counter
		running = true;
		pc = firstMemoryLocation;
		
		// If the pc is negative that means that there was no further jobs and nothing should execute
		if(pc < 0) return;
		
		while(pc <= 0xFF && running == true) // As long as running is true and the program counter is within a possible range
		{
			// Convert the integer stored in MEM to a hex string so it is easier to get the bits I want
			String word = Integer.toHexString(mem.memoryAction(MEMORY.READ, pc, 0));
			while(word.length() < 8) word = "0" + word; // Padds the  integer value to the correct 8 hex digit length
			
			// Gets the first bit and sets indirect as true if it is 1 and false if it is 0
			indirect = (Integer.parseInt(word.substring(0, 1), 16) >>> 3) == 1;
			
			// Get the first two hex digits and then strip off the first bit (the indexing bit) leaveing just the seven bit op code
			opCode = (Integer.parseInt(word.substring(0, 2), 16) & 0b01111111);
			
			// Gets the next hex digit which is the arithmetic register address
			arithRegisterAddress = Integer.parseInt(word.substring(2, 3), 16);
			
			// Gets the next hex digit which is the index register
			indexRegisterAddress = Integer.parseInt(word.substring(3, 4), 16);
			
			// Gets the last four hex digits which are the operand address
			ea = Integer.parseInt(word.substring(4), 16);
			
			// If indirect indexing and indexing
			if(indirect && indexRegisterAddress > 0) ea = arithRegister[indexRegisterAddress] + mem.memoryAction(MEMORY.READ, ea, 0);
			// Sets ea to the contents of MEM[ea] if indirect indexing is in use
			else if(indirect)ea = mem.memoryAction(MEMORY.READ, ea, 0);
			// If indiexing alone is used
			else if(indexRegisterAddress > 0) ea = arithRegister[indexRegisterAddress];
			
			// A switch that calls the correct method based on the op code
			switch (opCode)
			{
			case 0x00: halt(); // "00" = The halt operation
				break;
				
			case 0x01: load(); // "01" = The load operation
				break;
			
			case 0x02: store(); // "02" = The store operation
				break;
			
			case 0x03: add(); // "03" = The add operation
				break;
			
			case 0x04: subtract(); // "04" = The subtract operation
				break;
			
			case 0x05: multiply(); // "05" = The multiply operation
				break;
			
			case 0x06: divide(); // "06" = The divide operation
				break;
			
			case 0x07: shiftLeft(); // "07" = The binary shift left operation
				break;
			
			case 0x08: shiftRight(); // "08" = The binary shift left operation
				break;
			
			case 0x09: branchOnMinus(); // "09" = The branch on minus operation
				break;
			
			case 0x0A: branchOnPlus(); // "0A" = The branch on plus operation
				break;
			
			case 0x0B: branchOnZero(); // "0B" = The branch on zero operation
				break;
			
			case 0x0C: branchAndLink(); // "0C" = The branch and link operation
				break;
			
			case 0x0D: and(); // "0D" = The and operation
				break;
			
			case 0x0E: or(); // "0E" = The or operation
				break;
				
			case 0x0F: read(); // "0F" = The read operation
				break;
			
			case 0x10: write(); // "10" = The write operation
				break;
			
			case 0x11: dumpMemory(); // "11" = The dump memory operation
				break;

			default: error(); // error() is called if the op code is not one of the accepted codes
				break;
			}
		}
	}

	/**
	 * An error method that is called when the op code is not on the list
	 * @throws IOException
	 */
	private void error() throws IOException
	{
		// Send out an error message and then end this job
		System.out.println("Error: Invalid operation code");
		SYSTEM.output.write("Error: Invalid operation code" + nl);
		running = false;		
	}

	/**
	 * Method called when the op code is for a memory dump
	 * @throws IOException
	 */
	private void dumpMemory() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nDump Memory");
			SYSTEM.trace.write("BEFORE" + nl + "Dump Memory" + nl);
		}
		
		mem.memoryAction(MEMORY.DUMP, 0, 0); // Call to MEMORY to dump the contents of MEM
		pc++; // Increment the program counter
		SYSTEM.CLOCK++; // Increment the SYSTEM clock
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("Memory Dump Successful\n");
			SYSTEM.trace.write("Memory Dump Successful" + nl + nl);
		}
	}

	private void write() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nWrite\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Write" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		String hold = ""; // A string used to build the output string
		// Grab the contents of ea-ea+3 from MEM and appends them to hold
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 3, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 2, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 1, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 0, 0)));
		// Send hold out now that is has the appropriate string
		System.out.println("PROGRAM OUTPUT: " + hold);
		SYSTEM.output.write("PROGRAM OUTPUT: " + hold + nl);
		
		SYSTEM.CLOCK += 10; // As an expensive operation system clock is incremented by 10
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}


	/**
	 * Method called when the op code is for a read operation
	 * @throws IOException
	 */
	private void read() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nRead\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Read" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		Scanner scan = new Scanner(System.in);  // Creates a new scanner with which to get input
		System.out.print("System is requesting input: "); // Lets the user know that input is requested
		SYSTEM.output.write("System is requesting input: ");
		String input = scan.nextLine();  // Gets a line of input from the user, we will check the input's correctness later
		SYSTEM.output.write(input + nl);
		if(input.length() > 32)  // The function only takes in 32 hex digits, so if the input is longer than this it's wrong
		{
			// If the input is to long then we need to exit this job and give control back to SYSTEM
			System.out.println("Error: input can be no longer than 32 hexadecimal digits");
			SYSTEM.output.write("Error: input can be no longer than 32 hexadecimal digits" + nl);
			running = false;
			return;
		}
		else // Input isn't too long so we can move on
		{
			while(input.length() < 32) // The input needs to be 32 hex digits so we may need to pad a bit with 0s
			{
				input = "0" + input;
			}
		}
		try
		{
			Integer.parseInt(input, 16);  // Checks to make sure that we actually have hexidecimal input
		} catch (Exception e)  // If we don't then we catch that and exit to SYSTEM
		{
			System.out.println("Error: Invalid input, input must be in hexadecimal");
			SYSTEM.output.write("Error: Invalid input, input must be in hexadecimal" + nl);
			running = false;
			return;
		}
		// Here we load the input into memory now that we know the input is all good
		mem.memoryAction(MEMORY.WRITE, ea + 0, Integer.parseInt(input.substring(24), 16));
		mem.memoryAction(MEMORY.WRITE, ea + 1, Integer.parseInt(input.substring(16, 24), 16));
		mem.memoryAction(MEMORY.WRITE, ea + 2, Integer.parseInt(input.substring(8, 16), 16));
		mem.memoryAction(MEMORY.WRITE, ea + 3, Integer.parseInt(input.substring(0, 8), 16));
		
		// Finally we increment the clock by 10 since this is an expensive procedure, and the pc goes up by one
		SYSTEM.CLOCK += 10;
		pc++;
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for an or operation
	 * @throws IOException
	 */
	private void or() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nOr\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Or" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// Sets the register location to the value of that register location or'd with the specified location in MEM
		arithRegister[arithRegisterAddress] = 
				(arithRegister[arithRegisterAddress] | mem.memoryAction(MEMORY.READ, ea, 0));
		SYSTEM.CLOCK++; // Increment system the clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for an and operation
	 * @throws IOException
	 */
	private void and() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nAnd\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "And" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// Sets the contents of the specified location in the register to the result of anding those contents with the specified location in MEM
		arithRegister[arithRegisterAddress] = 
				(arithRegister[arithRegisterAddress] & mem.memoryAction(MEMORY.READ, ea, 0));
		SYSTEM.CLOCK++; // Increments the system clock
		pc++; // Increments the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is  for a branch and link operation
	 * @throws IOException
	 */
	private void branchAndLink() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBranch and Link\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch and Link" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = pc; // The program counter is stored in the specified locaiton in the register
		pc = ea; // Sets the program counter the the value for the effective area
		
		SYSTEM.CLOCK += 2; // As a slightly costly operation we increment the system clock by 2
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for the branch on zero operation
	 * @throws IOException
	 */
	private void branchOnZero() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBranch on Zero\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Zero" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] == 0) // Checks to see if the contents of the register location are 0
		{
			pc = ea; // If so then we set the program counter to the value of effective area
		}
		else pc++; // If not we increment the program counter like normal
		
		SYSTEM.CLOCK++; // Increment the system clock
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a branch on plus operation
	 * @throws IOException
	 */
	private void branchOnPlus() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBranch on Plus\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Plus" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] > 0) // Checks to see if the contents of the register at the specified location are greater than 0
		{
			pc = ea; // If so then we set the program counter to the value of effective area
		}
		else pc++; // If not we increment the program counter like normal
		
		SYSTEM.CLOCK++; // increment the system clock
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a branch on minus operation
	 * @throws IOException
	 */
	private void branchOnMinus() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBranch on Minus\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Minus" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] < 0) // Checks to see if the contents of the register at the specified location are less than 0
		{
			pc = ea; // If so we set the program counter to the value of the effective area
		}
		else pc++; // Otherwise we increment the program counter like normal
		
		SYSTEM.CLOCK++; // Increment the system clock
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a binary shift right operation
	 * @throws IOException
	 */
	private void shiftRight() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBinary Shift Right\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Binary Shift Right" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// Shift the contents of the register at the specified location right by ea places
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] >>> ea;
		
		SYSTEM.CLOCK++; // Increment system clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a binary shift left operation
	 * @throws IOException
	 */
	private void shiftLeft() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nBinary Shift Left\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Binary Shift Left" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// There is no left shift with no wrap ion java so I have to & the left shifted contents with 11111111 to get just 8 bits
		arithRegister[arithRegisterAddress] = 
				((arithRegister[arithRegisterAddress] << ea) & 0b11111111);
		
		SYSTEM.CLOCK++; // Increment the system clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a division operation
	 * @throws IOException
	 */
	private void divide() throws IOException
	{
		if(SYSTEM.TRACE)
		{ // Debugging message sent out when trace is on
			System.out.println("BEFORE\nDivide\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Divide" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0); // Loads in the operand
		if(operand == 0) // Check to make sure that the user it not trying to divide by zero
		{
			// If we are we  send out an error message and shut down this job
			System.out.println("Error: Division by zero. Congratulations you have doomed us all");
			SYSTEM.output.write("Error: Division by zero. Congratulations you have doomed us all" + nl);
			running = false;
			return;
		}
		// The contents of the register at the specified location are set to the value of the register contents divided by the operand from MEM
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] / operand;
		
		SYSTEM.CLOCK += 2; // As a slightly costly operation we increment the system clock by two
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a multiplicatin operation
	 * @throws IOException
	 */
	private void multiply() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nMultiply\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Multiply" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}

		int operand = mem.memoryAction(MEMORY.READ, ea, 0); // The specified operand is loaded from MEM
		// Checks to  make sure that the operation won't crate a number that is too large for the 32 bit limit
		if((long)arithRegister[arithRegisterAddress] * (long)operand > MAX_WORD_VALUE)
		{
			// If it is too large we send out a message and then exit this job
			System.out.println("Error: Buffer overflow has occurred - Multiply");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Multiply" + nl);
			running = false;
			return;
		}
		// If the operation is safe then it is gone ahead with
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] * operand;
		
		SYSTEM.CLOCK += 2; // As a slightly expensive operation we increment the system clock by 2
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method that is called when the op code is for a subtraction operation
	 * @throws IOException
	 */
	private void subtract() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nSubtract\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Subtract" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0); // A variable to hold the operand
		// Check to make sure that the operation can still fit in the 32 bit limitation
		if((long)arithRegister[arithRegisterAddress] - (long)operand < MIN_WORD_VALUE)
		{
			// If not an error message is sent out and the job is exited
			System.out.println("Error: Buffer overflow has occurred - Subtract");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Subtract" + nl);
			running = false;
			return;
		}
		// If the operation checks out it is carried out
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] - operand;
		
		SYSTEM.CLOCK++; // Increment the system clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for an addition operation
	 * @throws IOException
	 */
	private void add() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nAdd\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Add" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0); // A variable to hold the operand
		// Check to make sure that the result of the operation can fit in the 32 bit limit
		if((long)arithRegister[arithRegisterAddress] + (long)operand > MAX_WORD_VALUE)
		{
			// If not then an error message is sent out and the job is exited
			System.out.println("Error: Buffer overflow has occurred - Add");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Add" + nl);
			running = false;
			return;
		}
		// If the operatoin checks out then it is carried out
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] + operand;
		
		SYSTEM.CLOCK++; // Increment the system clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method that is called when the op code is for a store operation
	 * @throws IOException
	 */
	private void store() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nStore\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Store" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) +  nl);
		}
		
		// Stores the contents of the register at the specified location into MEM at location ea
		mem.memoryAction(MEMORY.WRITE, ea, arithRegister[arithRegisterAddress]);
		
		SYSTEM.CLOCK++; // Increments the system clock
		pc++; // Increments the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a load operation
	 * @throws IOException
	 */
	private void load() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("BEFORE\nLoad\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Load" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// Writes the content of MEM at address ea into the register at the specified location
		arithRegister[arithRegisterAddress] = mem.memoryAction(MEMORY.READ, ea, 0);
		
		SYSTEM.CLOCK++; // Increment the system clock
		pc++; // Increment the program counter
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	/**
	 * Method called when the op code is for a halt operation
	 * @throws IOException
	 */
	private void halt() throws IOException
	{
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("ALERT\nHalt\n");
			SYSTEM.trace.write("ALERT" + nl + "Halt" + nl + nl);
		}
		
		running = false; // When halt is called the current job is done so running is set to false to signify an end to the current job
		
		if(SYSTEM.TRACE) // Debugging message sent out when trace is on
		{
			System.out.println("Halt Successful\nJOB END\n\n\n");
			SYSTEM.trace.write("Halt Successful" + nl + "JOB END" + nl + nl + nl + nl);
		}
	}
	
	/**
	 * Method that takes a string and pads it with a number of "0" as the beginning until the string it 8 digits long
	 * @param hexString The string to be padded
	 * @return The padded string
	 */
	private String padd(String hexString)
	{
		String hold = ""; // A remporary string variable to work with rather than editing the original
		hold += hexString; // Appends the string that was passed in to the temporary string
		while(hold.length() < 8) hold = "0" + hold; // Padd the string with "0"s until it is 8 characters long
		return hold; // Now that the string is padded to the correct length it is returned
	}
	
}
