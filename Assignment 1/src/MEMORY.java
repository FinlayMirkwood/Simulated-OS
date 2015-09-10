/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/3/15
 */
public class MEMORY 
{
	public static String READ = "READ";
	public static String WRITE = "WRIT";
	public static String DUMP = "DUMP";
	
	
	private int[] MEM;
	
	public MEMORY() 
	{
		MEM = new int[0xff + 0x1];  // For the range to 0-FF we need FF+1 potential entries in the array
	}
	
	/**
	 * This method is used to interact with the "system memory" and is the main interface for the MEMORY class
	 * @param x The action to be taken
	 * @param y The memory address
	 * @param z The variable to be written into memory, or read into from memory
	 * @return the contents of memory at position y if x = READ
	 */
	public int memoryAction(String x, int y, int z)
	{
		if(x.equals(READ))
		{
			return MEM[y];  // Has to return a value since you can't assign back into int values across classes
		}
		if(x.equals(WRITE))
		{
			MEM[y] = z;  // Writes z into MEM at position y
		}
		if(x.equals(DUMP))
		{
			dumpMemory();  // Writes out the contents of MEM in the specified arrangement
		}
		return 0;
	}

	private void dumpMemory()
	{
		for(int i = 0; i < 32; i++)
		{
			System.out.printf("%04x", i*8);
			System.out.print("   ");
			for(int j = 0; j < 8; j++)
			{
				System.out.printf("%08x", MEM[(i*8) + j]);
				System.out.print(" ");
			}
			System.out.println();
		}
		
	}
}
