import java.util.Scanner;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/3/15
 */
public class CPU 
{
	private int opCode, arithRegisterAddress, indexRegisterAddress, operandAddress, pc;
	
	private boolean indirect, running;
	
	private int[] arithRegister, indexRegister;
	
	private MEMORY mem;
	
	CPU(MEMORY mem)
	{
		this.mem = mem;
		
		opCode = 0;
		arithRegisterAddress = 0;
		indexRegisterAddress = 0;
		operandAddress = 0;
		pc = 0;
		
		indirect = false;
		running = false; 
		
		arithRegister = new int[0xf];
		indexRegister = new int[0xf];
	}

	public void execute(int firstMemoryLocation) 
	{
		running = true;
		pc = firstMemoryLocation;
		
		// If the pc is negative that means that there was no further jobs and nothing should execute
		if(pc < 0) return;
		
		while(pc <= 0xFF && running == true)
		{
			// Convert the integer stored in MEM to a hex string so it is easier to get the bits I want
			String word = Integer.toHexString(mem.memoryAction(MEMORY.READ, pc, 0));
			
			// Gets the first bit and sets indirect as true if it is 1 and false if it is 0
			indirect = (Integer.parseInt(word.substring(0, 1), 16) >>> 3) == 1;
			
			// Get the first two hex digits and then strip off the first bit (the indexing bit) leaveing just the seven bit op code
			opCode = (Integer.parseInt(word.substring(0, 2), 16) & 0b01111111);
			
			// Gets the next hex digit which is the arithmetic register address
			arithRegisterAddress = Integer.parseInt(word.substring(2, 3), 16);
			
			// Gets the next hex digit which is the index register
			indexRegisterAddress = Integer.parseInt(word.substring(3, 4), 16);
			
			// Gets the last four hex digits which are the operand address
			operandAddress = Integer.parseInt(word.substring(4), 16);
			
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

	private void error()
	{
		System.out.println("Error: Invalid operation code");
		running = false;		
	}

	private void dumpMemory()
	{
		mem.memoryAction(MEMORY.DUMP, 0, 0);
		pc++;
		SYSTEM.CLOCK++;
	}

	private void write()
	{
		String hold = "";
		hold += Integer.toHexString(mem.memoryAction(MEMORY.READ, operandAddress + 3, 0));
		hold += Integer.toHexString(mem.memoryAction(MEMORY.READ, operandAddress + 2, 0));
		hold += Integer.toHexString(mem.memoryAction(MEMORY.READ, operandAddress + 1, 0));
		hold += Integer.toHexString(mem.memoryAction(MEMORY.READ, operandAddress + 0, 0));
		System.out.println(hold);
		SYSTEM.CLOCK += 10;
		pc++;
	}

	private void read()
	{
		Scanner scan = new Scanner(System.in);  // Creates a new scanner with which to get input
		String input = scan.nextLine();  // Gets a line of input from the user, we will check the input's correctness later
		if(input.length() > 32)  // The function only takes in 32 hex digits, so if the input is longer than this it's wrong
		{
			// If the input is to long then we need to exit this job and give control back to SYSTEM
			System.out.println("Error: input can be no longer than 32 hexadecimal digits");
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
			running = false;
			return;
		}
		// Here we load the input into memory now that we know the input is all good
		mem.memoryAction(MEMORY.WRITE, operandAddress + 0, Integer.parseInt(input.substring(24), 16));
		mem.memoryAction(MEMORY.WRITE, operandAddress + 1, Integer.parseInt(input.substring(16, 24), 16));
		mem.memoryAction(MEMORY.WRITE, operandAddress + 2, Integer.parseInt(input.substring(8, 16), 16));
		mem.memoryAction(MEMORY.WRITE, operandAddress + 3, Integer.parseInt(input.substring(0, 8), 16));
		
		// Finally we increment the clock by 10 since this is an expensive procedure, and the pc goes up by one
		SYSTEM.CLOCK += 10;
		pc++;
	}

	private void or()
	{
		// TODO Auto-generated method stub
		SYSTEM.CLOCK++;
		pc++;
	}

	private void and()
	{
		// TODO Auto-generated method stub
		
	}

	private void branchAndLink()
	{
		// TODO Auto-generated method stub
		
	}

	private void branchOnZero()
	{
		// TODO Auto-generated method stub
		
	}

	private void branchOnPlus()
	{
		// TODO Auto-generated method stub
		
	}

	private void branchOnMinus()
	{
		// TODO Auto-generated method stub
		
	}

	private void shiftRight()
	{
		// TODO Auto-generated method stub
		
	}

	private void shiftLeft()
	{
		// TODO Auto-generated method stub
		
	}

	private void divide()
	{
		// TODO Auto-generated method stub
		
	}

	private void multiply()
	{
		// TODO Auto-generated method stub
		
	}

	private void subtract()
	{
		// TODO Auto-generated method stub
		
	}

	private void add()
	{
		// TODO Auto-generated method stub
		
	}

	private void store()
	{
		// TODO Auto-generated method stub
		
	}

	private void load()
	{
		// TODO Auto-generated method stub
		
	}

	private void halt()
	{
		running = false;		
	}
	
}
