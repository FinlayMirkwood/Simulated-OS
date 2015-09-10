import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/5/15
 */
public class LOADER 
{
	private int firstRecordLocation;
	private int lastRecordLocation;
	private int dataLength;
	
	private boolean foundNextJob;
	
	private int[] dataWords;
	
	public Scanner scan;
	
	private MEMORY mem;
	
	LOADER(MEMORY mem) 
	{
		this.mem = mem;
		
		File f = new File("res/test.txt");
		try
		{
			scan = new Scanner(f);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		foundNextJob = false;
		
		firstRecordLocation = 0;
		lastRecordLocation = 0;
		dataLength = 0;
	}

	public int loadNextJob() 
	{
		foundNextJob = getFirstLine(); // Gets the first line with the first record location and length
		if(foundNextJob)
		{
			getDataWords(); // Gets the data words that make up the instructions
			loadDataWords(); // Loads the data words that make up the instructions into MEM
			getLastLine(); // Gets the last line of instructions that contains the last record start address and the trace flag
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
	
	public void getLastLine()
	{
		String temp = scan.nextLine();
		lastRecordLocation = Integer.parseInt(temp.substring(0, 2), 16);
		if(Integer.parseInt(temp.substring(3), 16) > 0) SYSTEM.TRACE = true;
		else SYSTEM.TRACE = false;
	}
	
	public void getDataWords()
	{
		dataWords = new int[dataLength];
		int counter = 0;
		String currentLine = "";
		for(int i = 0; i < dataLength / 4; i++)
		{
			currentLine = scan.nextLine();
			for(int j = 0; j < currentLine.length(); j += 8)
			{
				dataWords[counter] = Integer.parseInt(currentLine.substring(j, j+8), 16);
				counter++;
			}
		}
	}
	
	public void loadDataWords()
	{
		for(int i = 0; i < dataWords.length; i++)
		{
			mem.memoryAction(MEMORY.WRITE, firstRecordLocation + i, dataWords[i]);
		}
	}

}







