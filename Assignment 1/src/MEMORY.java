import java.io.IOException;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/12/15
 */
public class MEMORY 
{
	// These variables hold the codes for the different actions that MEMORY can take
	public static final String READ = "READ";
	public static final String WRITE = "WRIT";
	public static final String DUMP = "DUMP";
	
	private String nl = System.getProperty("line.separator"); // The new line character for use when  writing to a file
	
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
	 * @throws IOException 
	 */
	public int memoryAction(String x, int y, int z) throws IOException
	{
		if(x.equals(READ)) // The read action is requested
		{
			try
			{
				return MEM[y]; // Has to return a value since you can't assign back into int values across classes
			} catch (IndexOutOfBoundsException e)
			{
				// If there is an Index Out of Bounds Exception then a message is sent out, but execution continues
				System.out.println("Error: Index Out of Bournds Exception. Read operation has failed. Program may have unexpected results.");
				SYSTEM.output.write("Error: Index Out of Bournds Exception. Read operation has failed. Program may have unexpected results." + nl);
			}  
		}
		if(x.equals(WRITE)) // The write action is requested
		{
			try
			{
				MEM[y] = z;  // Writes z into MEM at position y
			} catch (IndexOutOfBoundsException e)
			{
				// If there is an Index Out of Bounds Exception then a message is sent out, but execution continues
				System.out.println("Error: Index Out of Bournds Exception. Write operation has failed. Program may have unexpected results.");
				SYSTEM.output.write("Error: Index Out of Bournds Exception. Write operation has failed. Program may have unexpected results." + nl);
			}
		}
		if(x.equals(DUMP)) // The dump action is requested
		{
			dumpMemory();  // Writes out the contents of MEM in the specified arrangement
		}
		return 0;
	}

	private void dumpMemory() throws IOException
	{
		for(int i = 0; i < 32; i++) // There should be 32 rows in the output
		{
			// Outputs the row label
			System.out.printf("%04x", i*8); 
			SYSTEM.output.write(String.format("%04x", i*8));
			System.out.print("   ");
			SYSTEM.output.write("   ");
			for(int j = 0; j < 8; j++) // There are 8 words in each row
			{
				// Outputs the words for each row
				System.out.printf("%08x", MEM[(i*8) + j]);
				SYSTEM.output.write(String.format("%08x", MEM[(i*8) + j]));
				System.out.print(" ");
				SYSTEM.output.write(" ");
			}
			// Outputs a new row to improve readability
			System.out.println();
			SYSTEM.output.write(nl);
		}
		
	}
}
