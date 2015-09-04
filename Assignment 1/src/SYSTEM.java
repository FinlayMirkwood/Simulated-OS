/**
 * 
 * @author Thomas Elswick
 * @date 9/3/15
 * @updated 9/3/15
 */
public class SYSTEM 
{
	public static boolean LAST_JOB;
	
	
	public static void main(String[] args)
	{
		LAST_JOB = false;
		
		LOADER lod = new LOADER();
		MEMORY mem = new MEMORY();
		CPU cpu = new CPU();
		
		while(LAST_JOB == false)
		{
			cpu.execute(lod.loadNextJob());
		}
		
	}
	

}
