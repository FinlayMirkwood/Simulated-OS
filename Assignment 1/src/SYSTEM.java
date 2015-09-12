import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

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
	
	
	public static void main(String[] args) throws IOException
	{
		CLOCK = 0;  
		
		LAST_JOB = false;
		TRACE = false;
		
		outputFile.setWritable(true);
		traceFile.setWritable(true);
		output = new FileWriter(outputFile);
		trace = new FileWriter(traceFile);
		
		
		MEMORY mem = new MEMORY();
		LOADER lod = new LOADER(mem);
		CPU cpu = new CPU(mem);
		
		while(LAST_JOB == false)
		{
			cpu.execute(lod.loadNextJob());
			System.out.println("Clock: " + CLOCK);
			SYSTEM.output.write("Clock: " + CLOCK + nl);
		}
		output.close();
		trace.close();
	}
	

}
