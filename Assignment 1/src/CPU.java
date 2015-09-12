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
	
	private int opCode, arithRegisterAddress, indexRegisterAddress, ea, pc;
	
	private boolean indirect, running;
	
	private int[] arithRegister, indexRegister;
	
	private String nl = System.getProperty("line.separator");
	
	private MEMORY mem;
	
	CPU(MEMORY mem)
	{
		this.mem = mem;
		
		opCode = 0;
		arithRegisterAddress = 0;
		indexRegisterAddress = 0;
		ea = 0;
		pc = 0;
		
		indirect = false;
		running = false; 
		
		arithRegister = new int[0xf];
		indexRegister = new int[0xf];
	}

	public void execute(int firstMemoryLocation) throws IOException 
	{
		running = true;
		pc = firstMemoryLocation;
		
		// If the pc is negative that means that there was no further jobs and nothing should execute
		if(pc < 0) return;
		
		while(pc <= 0xFF && running == true)
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
			
			
			switch (opCode)
			{
			case 0x00: halt();				
				break;
				
			case 0x01: load();				
				break;
			
			case 0x02: store();				
				break;
			
			case 0x03: add();				
				break;
			
			case 0x04: subtract();				
				break;
			
			case 0x05: multiply();				
				break;
			
			case 0x06: divide();				
				break;
			
			case 0x07: shiftLeft();				
				break;
			
			case 0x08: shiftRight();				
				break;
			
			case 0x09: branchOnMinus();				
				break;
			
			case 0x0A: branchOnPlus();				
				break;
			
			case 0x0B: branchOnZero();				
				break;
			
			case 0x0C: branchAndLink();				
				break;
			
			case 0x0D: and();				
				break;
			
			case 0x0E: or();				
				break;
				
			case 0x0F: read();				
				break;
			
			case 0x10: write();				
				break;
			
			case 0x11: dumpMemory();				
				break;

			default: error();
				break;
			}
		}
		
	}

	private void error() throws IOException
	{
		System.out.println("Error: Invalid operation code");
		SYSTEM.output.write("Error: Invalid operation code" + nl);
		running = false;		
	}

	private void dumpMemory() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nDump Memory\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Dump Memory" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		mem.memoryAction(MEMORY.DUMP, 0, 0);
		pc++;
		SYSTEM.CLOCK++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void write() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nWrite\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Write" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		String hold = "";
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 3, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 2, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 1, 0)));
		hold += padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea + 0, 0)));
		System.out.println("PROGRAM OUTPUT: " + hold);
		SYSTEM.output.write("PROGRAM OUTPUT: " + hold + nl);
		SYSTEM.CLOCK += 10;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}


	private void read() throws IOException
	{
		if(SYSTEM.TRACE)
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
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void or() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nOr\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Or" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = 
				(arithRegister[arithRegisterAddress] | mem.memoryAction(MEMORY.READ, ea, 0));
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void and() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nAnd\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "And" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = 
				(arithRegister[arithRegisterAddress] & mem.memoryAction(MEMORY.READ, ea, 0));
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void branchAndLink() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBranch and Link\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch and Link" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = pc;
		pc = ea;
		
		SYSTEM.CLOCK += 2;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void branchOnZero() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBranch on Zero\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Zero" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] == 0)
		{
			pc = ea;
		}
		else pc++;
		
		SYSTEM.CLOCK++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void branchOnPlus() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBranch on Plus\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Plus" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] > 0)
		{
			pc = ea;
		}
		else pc++;
		
		SYSTEM.CLOCK++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void branchOnMinus() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBranch on Minus\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Branch on Minus" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(arithRegister[arithRegisterAddress] < 0)
		{
			pc = ea;
		}
		else pc++;
		
		SYSTEM.CLOCK++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void shiftRight() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBinary Shift Right\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Binary Shift Right" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] >>> ea;
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void shiftLeft() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nBinary Shift Left\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Binary Shift Left" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		// There is no left shift with no wrap ion java so I have to & the left shifted contents with 11111111 to get just 8 bits
		arithRegister[arithRegisterAddress] = 
				((arithRegister[arithRegisterAddress] << ea) & 0b11111111);
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void divide() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nDivide\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Divide" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0);
		// Check to make sure that the user it not trying to divide by zero
		if(operand == 0)
		{
			System.out.println("Error: Division by zero. Congratulations you have doomed us all");
			SYSTEM.output.write("Error: Division by zero. Congratulations you have doomed us all" + nl);
			running = false;
			return;
		}
		if((long)arithRegister[arithRegisterAddress] / (long)operand > MAX_WORD_VALUE)
		{
			System.out.println("Error: Buffer overflow has occurred - Divide");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Divide" + nl);
			running = false;
			return;
		}
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] / operand;
		
		SYSTEM.CLOCK += 2;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void multiply() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nMultiply\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Multiply" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nPC: " + pc + " A: " + arithRegister[arithRegisterAddress]);
			SYSTEM.trace.write("BEFORE" + nl + "PC: " + pc + " A: " + arithRegister[arithRegisterAddress] + nl);
		}
		int operand = mem.memoryAction(MEMORY.READ, ea, 0);
		if((long)arithRegister[arithRegisterAddress] * (long)operand > MAX_WORD_VALUE)
		{
			System.out.println("Error: Buffer overflow has occurred - Multiply");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Multiply" + nl);
			running = false;
			return;
		}
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] * operand;
		
		SYSTEM.CLOCK += 2;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void subtract() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nSubtract\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Subtract" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0);
		if((long)arithRegister[arithRegisterAddress] * (long)operand < MIN_WORD_VALUE)
		{
			System.out.println("Error: Buffer overflow has occurred - Subtract");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Subtract" + nl);
			running = false;
			return;
		}
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] - operand;
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void add() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nAdd\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Add" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		int operand = mem.memoryAction(MEMORY.READ, ea, 0);
		if((long)arithRegister[arithRegisterAddress] * (long)operand > MAX_WORD_VALUE)
		{
			System.out.println("Error: Buffer overflow has occurred - Add");
			SYSTEM.output.write("Error: Buffer overflow has occurred - Add" + nl);
			running = false;
			return;
		}
		arithRegister[arithRegisterAddress] = arithRegister[arithRegisterAddress] + operand;
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void store() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nStore\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Store" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) +  nl);
		}
		
		mem.memoryAction(MEMORY.WRITE, ea, arithRegister[arithRegisterAddress]);
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void load() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("BEFORE\nLoad\nPC: " + padd(Integer.toHexString(pc)) + " A: " 
		+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])));
			SYSTEM.trace.write("BEFORE" + nl + "Load" + nl + "PC: " + padd(Integer.toHexString(pc)) + " A: " 
					+ padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + nl);
		}
		
		arithRegister[arithRegisterAddress] = mem.memoryAction(MEMORY.READ, ea, 0);
		
		SYSTEM.CLOCK++;
		pc++;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("AFTER\nA: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + "\n");
			SYSTEM.trace.write("AFTER" + nl + "A: " + padd(Integer.toHexString(arithRegister[arithRegisterAddress])) + 
					" EA: " + padd(Integer.toHexString(mem.memoryAction(MEMORY.READ, ea, 0))) + nl + nl);
		}
	}

	private void halt() throws IOException
	{
		if(SYSTEM.TRACE)
		{
			System.out.println("ALERT\nHalt\n");
			SYSTEM.trace.write("ALERT" + nl + "Halt" + nl + nl);
		}
		
		running = false;
		
		if(SYSTEM.TRACE)
		{
			System.out.println("Halt Successful\nJOB END\n\n\n");
			SYSTEM.trace.write("Halt Successful" + nl + "JOB END" + nl + nl + nl + nl);
		}
	}
	
	private String padd(String hexString)
	{
		String hold = "";
		hold += hexString;
		while(hold.length() < 8) hold = "0" + hold;
		return hold;
	}
	
}
