import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProviderGenerator {
	public static void main(String[] args) throws IOException {
		
		generate("data/test");
		
	}
	
	public static void generate(String filename) throws IOException {
		int nbTasks, nbRenewable, nbNonRenewable;
		DataReader data = new DataReader(filename);
		try {
			
	    	nbTasks = data.next();
		    nbRenewable = data.next();
		    nbNonRenewable = data.next();
		    
		    generateEnterprise(500, 600, 20);
		    
		    
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error: " + e);
		}
		
		 
	}
	
	public static void generateEnterprise(int width, int height, int amount) {
		List<Enterprise> eList = new ArrayList<Enterprise>();
		for (int i = 0; i < amount; i++) {
			Enterprise e = new Enterprise(width, height);
			eList.add(e);
			System.out.print(i + " : ");
			e.getLocation();
		}
	}
}
