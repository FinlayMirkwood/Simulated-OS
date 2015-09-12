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
	private int firstRecordLocation;
	private int lastRecordLocation;
	private int dataLength;
	
	private boolean foundNextJob, foundNextWords, foundNextLastLine;
	
	private int[] dataWords;
	
	private String nl = System.getProperty("line.separator");
	
	public Scanner scan;
	
	private MEMORY mem;
	
	LOADER(MEMORY mem) 
	{
		this.mem = mem;
		
		File f = new File("job.txt");
		try
		{
			scan = new Scanner(f);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		foundNextJob = false;
		foundNextWords = false;
		foundNextLastLine = false;
		
		firstRecordLocation = 0;
		lastRecordLocation = 0;
		dataLength = 0;
	}

	public int loadNextJob() throws IOException 
	{
		foundNextJob = getFirstLine(); // Gets the first line with the first record location and length
		if(foundNextJob)
		{
			foundNextWords = getDataWords(); // Gets the data words that make up the instructions
			if(foundNextWords)
			{
				loadDataWords(); // Loads the data words that make up the instructions into MEM
				foundNextLastLine = getLastLine(); // Gets the last line of instructions that contains the last record start address and the trace flag
				if(!foundNextLastLine)
				{
					SYSTEM.LAST_JOB = true;
					return -1;
				}
			}
			else
			{
				SYSTEM.LAST_JOB = true;
				return -1;
			}
		}
		else 
		{
			SYSTEM.LAST_JOB = true;
			return -1;
		}
		return firstRecordLocation;
	}
	
	public boolean getFirstLine()
	{
		String firstLine = "";
		String pattern = "[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{2}";
		String temp = "";
		
		while(firstLine.equals(""))
		{
			if(scan.hasNextLine())
			{
				temp = scan.nextLine();
				if(Pattern.matches(pattern, temp)) firstLine = temp;
			}
			else
			{
				return false;
			}
		}
		firstRecordLocation = Integer.parseInt(firstLine.substring(0, 2), 16);
		dataLength = Integer.parseInt(firstLine.substring(3), 16);
		return true;
	}
	
	public boolean getLastLine() throws IOException
	{
		String temp = "";
		String pattern = "[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{1}";
		if(scan.hasNextLine())
		{
			temp = scan.nextLine();
			if(Pattern.matches(pattern, temp))
			{
				lastRecordLocation = Integer.parseInt(temp.substring(0, 2), 16);
				if(Integer.parseInt(temp.substring(3), 16) > 0) SYSTEM.TRACE = true;
				else SYSTEM.TRACE = false;
			}
			else
			{
				System.out.println("Error: Improper instruction format - Last line");
				SYSTEM.output.write("Error: Improper instruction format - Last line" + nl);
				return false;
			}
			
		}
		return true;
	}
	
	public boolean getDataWords() throws IOException
	{
		dataWords = new int[dataLength];
		int counter = 0;
		String currentLine = "";
		for(int i = 0; i < dataLength / 4; i++)
		{
			currentLine = scan.nextLine();
			for(int j = 0; j < currentLine.length(); j += 8)
			{
				try
				{
					dataWords[counter] = Integer.parseInt(currentLine.substring(j, j+8), 16);
				} catch (Exception e)
				{
					System.out.println("Error: Improper instruction format - Instructions");
					SYSTEM.output.write("Error: Improper instruction format - Instructions" + nl);
					return false;
				}
				counter++;
			}
		}
		return true;
	}
	
	public void loadDataWords() throws IOException
	{
		for(int i = 0; i < dataWords.length; i++)
		{
			mem.memoryAction(MEMORY.WRITE, firstRecordLocation + i, dataWords[i]);
		}
	}

}







