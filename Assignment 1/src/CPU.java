/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/3/15
 */
public class CPU 
{
	private int opCode, registerAddress, indexRegister, operandAddress, pc;
	
	private boolean indirect, running;
	
	private MEMORY mem;
	
	CPU(MEMORY mem)
	{
		this.mem = mem;
		
		opCode = 0;
		registerAddress = 0;
		indexRegister = 0;
		operandAddress = 0;
		pc = 0;
		
		indirect = false;
		running = false; 
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
			registerAddress = Integer.parseInt(word.substring(2, 3), 16);
			
			// Gets the next hex digit which is the index register
			indexRegister = Integer.parseInt(word.substring(3, 4), 16);
			
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
		// TODO Auto-generated method stub
		
	}

	private void read()
	{
		// TODO Auto-generated method stub
		
	}

	private void or()
	{
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
	
}
