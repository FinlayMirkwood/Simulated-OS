/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/5/15
 */
public class SYSTEM 
{
	public static int CLOCK;
	
	public static boolean LAST_JOB;
	public static boolean TRACE;
	
	
	public static void main(String[] args)
	{
		CLOCK = 0;
		
		LAST_JOB = false;
		TRACE = false;
		
		MEMORY mem = new MEMORY();
		LOADER lod = new LOADER(mem);
		CPU cpu = new CPU(mem);
		
		while(LAST_JOB == false)
		{
			cpu.execute(lod.loadNextJob());
			System.out.println("Clock: " + CLOCK);
		}
		
	}
	

}
