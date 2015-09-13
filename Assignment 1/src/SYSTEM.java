import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/12/15
 */
public class SYSTEM 
{
	public static int CLOCK;  // A clock variable to keep track of simulated running time
	
	public static boolean LAST_JOB;  // Keeps track of whether or not we are on the last job in the job.txt file
	public static boolean TRACE;  // Gives access to whether or not the trace bit is on
	
	private static String nl = System.getProperty("line.separator");  // The new line character for writing to the output files
	
	public static File outputFile = new File("output.txt");  // A file to write output to
	public static File traceFile = new File("trace.txt");  // A file to write to if trace is true
	public static FileWriter output;  // A file writer for writing to the output file
	public static FileWriter trace;  // A file writer for writing to the trace file
	
	/**
	 * This is the main class of the simulation. It holds references to a number of resource variables
	 * as well as containing the main running loop.
	 */
	public static void main(String[] args) throws IOException
	{
		CLOCK = 0;  
		
		LAST_JOB = false;
		TRACE = false;
		
		//Setting the files as writable and then gets the writers ready to do their work
		outputFile.setWritable(true);
		traceFile.setWritable(true);
		output = new FileWriter(outputFile);
		trace = new FileWriter(traceFile);
		
		// Initializes the different components of the simulation
		MEMORY mem = new MEMORY();
		LOADER lod = new LOADER(mem);
		CPU cpu = new CPU(mem);
		
		// This is the main loop. LAST_JOB will be set false when it's time to stop operations
		// Note: In an acutal OS there might be some down time, but the system would never actually stop altogether
		while(LAST_JOB == false)
		{
			// lod loads the next job which is then executed by cpu
			cpu.execute(lod.loadNextJob());
			// Writing output to the system and to the output file
			System.out.println("Clock: " + CLOCK);
			SYSTEM.output.write("Clock: " + CLOCK + nl);
		}
		// Now that all jobs are done we need to flush close our writers
		output.flush();
		output.close();
		trace.flush();
		trace.close();
	}
}
