import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;




public class Testing {
	
	
	public static int firstRecordLocation = 0;
	public static int dataLength = 0;

	public static void main(String[] args) throws FileNotFoundException 
	{
//		Integer i = new Integer(0);
//		swap(i, 2);
//		
//		System.out.println(i);
		
//		int i = 0xFF;
//		for(int j = 0; j < 7; j++) 
//		{
//			i = i << 1;
//			i = i & 0b011111111;
//		}
//		
//		System.out.println(i);
		
		
		
		
//		getFirstLine();
//		System.out.println(firstRecordLocation + "  "  + dataLength);
		
//		int i = 15;
//		System.out.println(Integer.toHexString(i));
		
//		String s = "7";
//		if((Integer.parseInt(s.substring(0, 1), 16) >>> 3) == 1) System.out.println("inderict");
//		else if((Integer.parseInt(s.substring(0, 1), 16) >>> 3) == 0) System.out.println("direct");
//		else System.out.println("What?!?!");
		
//		String word = "FF";
//		System.out.println((Integer.parseInt(word.substring(0, 2), 16) & 0b01111111));
		
//		Scanner scan = new Scanner(System.in);
//		scan.useDelimiter("\n");
//		System.out.print(scan.next());
		
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		if(input.length() > 32) System.out.println("Error: input can be no longer than 32 hexadecimal digits");
		else
		{
			while(input.length() < 32)
			{
				input = "0" + input;
			}
		}
		try
		{
			Integer.parseInt(input, 16);
		} catch (Exception e)
		{
			System.out.println("Error: Invalid input, input must be in hexadecimal");
		}
		
	}
	
	private static void swap(int i, int updated)
	{
		i = new Integer(updated);
	}
	
	
	public static void getFirstLine() throws FileNotFoundException
	{
		File f = new File("res/test.txt");

		Scanner scan = new Scanner(f);
		
		String firstLine = "";
		String pattern = "[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{2}";
		
		while(firstLine.equals("") || firstLine == null)
		{
			firstLine = scan.findInLine("[0-9a-fA-F]{2}[ \t][0-9a-fA-F]{2}");
			scan.nextLine();
		}
		firstRecordLocation = Integer.parseInt(firstLine.substring(0, 2), 16);
		dataLength = Integer.parseInt(firstLine.substring(3), 16);
	}

}
