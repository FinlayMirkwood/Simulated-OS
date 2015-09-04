import java.io.File;




public class Testing {

	public static void main(String[] args) 
	{
		Integer i = new Integer(0);
		swap(i, 2);
		
		System.out.println(i);

	}
	
	private static void swap(int i, int updated)
	{
		i = new Integer(updated);
	}

}
