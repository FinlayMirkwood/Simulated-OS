import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/12/15
 */
public class LOADER 
{
	// Some integers used to keep track of some info extracted from the job file
	private int firstRecordLocation, lastRecordLocation, dataLength;
	
	// These booleans are used for some checks to see if the instructions are formatted correctly and can be found/parsed
	private boolean foundNextJob, foundNextWords, foundNextLastLine;
	
	// This array is used to hold the data words once they've been pulled in
	private int[] dataWords;
	
	private String nl = System.getProperty("line.separator"); // The new line character used for writing to the files
	
	public Scanner scan;  // A scanner used to scan through the job file
	
	private MEMORY mem; // A reference to the current instance of MEMORY for method calls
	
	/**
	 * The constructor for this instance of LOADER
	 * @param mem the instance of MEMORY to which this LOADER loads instructions 
	 * @throws IOException 
	 */
	LOADER(MEMORY mem) throws IOException 
	{
		this.mem = mem;
		
		File f = new File("job.txt"); // The file that contains the instructions with the jobs
		// This try catch block just makes sure that the file is actually there.
		try
		{
			scan = new Scanner(f);
		} catch (FileNotFoundException e)
		{
			// If no file is found there there's nothing to execute so a message is sent out and LAST_JOB becomes true
			System.out.println("Error: No job file found");
			SYSTEM.output.write("Error: No job file found" + nl);
			SYSTEM.LAST_JOB = true;
		}
		
		
		// Sets some default values for the follwoing variables
		foundNextJob = false;
		foundNextWords = false;
		foundNextLastLine = false;
		
		firstRecordLocation = 0;
		lastRecordLocation = 0;
		dataLength = 0;
	}

	/**
	 * Loads the next job from the job.txt file
	 * @return the first record location. Returns -1 if no properly formatted job is found
	 * @throws IOException
	 */
	public int loadNextJob() throws IOException 
	{
		// Gets the first line with the first record location and length. getFirstLine() returns false if no line is found, true if one is found
		foundNextJob = getFirstLine(); 
		if(foundNextJob)
		{
			// Gets the data words that make up the instructions. getDataWords() returns false if data lines are not correct, true if they are
			foundNextWords = getDataWords(); 
			if(foundNextWords)
			{
				loadDataWords(); // Loads the data words that make up the instructions into MEM
				// Gets the last line of instructions that contains the last record start address and the trace flag
				// getLastLine() returns false if no properly formatted line can be found, true if it can
				foundNextLastLine = getLastLine(); 
				if(!foundNextLastLine) // If no properly formatted last line was found then we cancel this job
				{
					return -1;
				}
			}
			else // If no properly formatted data words are found then we cancel this job
			{
				return -1;
			}
		}
		else // If the file contains no  properly formatted first line then we are done here and we stop operations
		{
			SYSTEM.LAST_JOB = true;
			return -1;
		}
		return firstRecordLocation; // This is used in the call cpu.execute(lod.loadNextJob()) so that cpu know where to start execution
	}
	
	/**
	 * Gets the first line of instructions from the job file
	 * @return returns true if a properly formatted first line is found, false otherwise
	 */
	public boolean getFirstLine()
	{
		String firstLine = ""; // A string to hold the first line once it is extracted
		String pattern = "[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{2}"; // The pattern for the first line. This equates to "two hex digits followed by a space followed by another two hex digits"
		String temp = "";
		
		// Tries to keep looping until we find a properly formatted first line and assign it to the variable "firstLine"
		while(firstLine.equals(""))
		{
			if(scan.hasNextLine()) // Checks to see if there is still another line in the job.txt file
			{
				temp = scan.nextLine(); // If there is we assign that line to temp
				// If the line matches the pattern for the first line then we have finally found our first line and we can assign it to "firstLine"
				if(Pattern.matches(pattern, temp)) firstLine = temp; 
			}
			else
			{
				return false; // If there are no more lines left in the file then we are out of jobs and we exit by returning false
			}
		}
		// One we have the first line can parse it into our variables and then return true
		firstRecordLocation = Integer.parseInt(firstLine.substring(0, 2), 16);
		dataLength = Integer.parseInt(firstLine.substring(3), 16);
		return true;
	}
	
	/**
	 * Gets the last line of a job from the job.txt file
	 * @return returns true if a properly formatted last line is found, false otherwise
	 * @throws IOException
	 */
	public boolean getLastLine() throws IOException
	{
		String temp = "";
		// The pattern for the last line of a job. This is basically "two hex digits followed by a space and then one more hex digit"
		String pattern = "[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{1}";
		if(scan.hasNextLine()) // Checks to make sure that there is another line in the file
		{
			temp = scan.nextLine(); // If there is another line then we assign it to temp
			if(Pattern.matches(pattern, temp)) // Checks to make see if the line matches the correct pattern
			{
				// If the last line is formatted correctly then we can parse it into the appropriate variables
				lastRecordLocation = Integer.parseInt(temp.substring(0, 2), 16);
				if(Integer.parseInt(temp.substring(3), 16) > 0) SYSTEM.TRACE = true;
				else SYSTEM.TRACE = false;
			}
			else // If the last line is not formatted correctly
			{
				// Send out the appropriate messages and return false
				System.out.println("Error: Improper instruction format - Last line");
				SYSTEM.output.write("Error: Improper instruction format - Last line" + nl);
				return false;
			}
			
		}
		else return false; // If there is not another line then the job is not formatted properly and false is returned
		return true; // If we have gotten to this point then there was a last line and it was formatted correctly so true is returned
	}
	
	/**
	 * Gets the data words from the job.txt file that contain the actual instructions for the job
	 * @return returns true if the contents are formatted correctly and can be extracted, false otherwise
	 * @throws IOException
	 */
	public boolean getDataWords() throws IOException
	{
		// We know from the first line how many words to expect so we make an array of the correct length to hold them
		dataWords = new int[dataLength]; 
		int counter = 0; // Counter variable used when parsing the instructions
		String currentLine = ""; // A variable to hold the line of data we are currently working on
		// We know the number of words, there are four words per line, so this works through "number-of-words/4" lines
		int rows = dataLength / 4;
		if(dataLength % 4 != 0) rows++;
		for(int i = 0; i < rows; i++)
		{
			if(scan.hasNextLine()) // Checks to make sure we have anoter line to work with
			{
			currentLine = scan.nextLine(); // Grabs the line we want to work with next
				for(int j = 0; j < currentLine.length(); j += 8) // Each word is 8 digits, so we increment by 8
				{
					try
					{
						// Tries to load the next word, if it fails then something is not set up correctly
						dataWords[counter] = Integer.parseInt(currentLine.substring(j, j+8), 16);
					} catch (Exception e)
					{
						// If the string is not in hex, or if there is some sort of out of bounds error then we exit
						System.out.println("Error: Improper instruction format - Instructions");
						SYSTEM.output.write("Error: Improper instruction format - Instructions" + nl);
						return false;
					}
					counter++;
				}
			}
			else
			{
				// If there is not another line then the instructions are not formatted properly and we exit
				System.out.println("Error: Improper instruction format - Instructions");
				SYSTEM.output.write("Error: Improper instruction format - Instructions" + nl);
				return false;
			}
		}
		return true; // If the program gets here then everything worked correctly and true is returned
	}
	
	/**
	 * Loads the data from the job.txt file into MEM through an instance of MEMORY
	 * @throws IOException
	 */
	public void loadDataWords() throws IOException
	{
		for(int i = 0; i < dataWords.length; i++) // Loops through the memory words that should have already been extraced
		{
			mem.memoryAction(MEMORY.WRITE, firstRecordLocation + i, dataWords[i]); // Loads them into MEM through mem
		}
	}
}