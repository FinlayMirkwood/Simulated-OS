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
	
	public int memoryAction(String x, int y, int z)
	{
		if(x.equals(READ))
		{
			return MEM[y];
		}
		if(x.equals(WRITE))
		{
			MEM[y] = z;
		}
		if(x.equals(DUMP))
		{
			dumpMemory();
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
