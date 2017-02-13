import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProviderGenerator {
	public static void main(String[] args) throws IOException {
		
		generate("data/test");
		
	}
	
	public static void generate(String filename) throws IOException {
		int nbTasks, nbRenewable, nbNonRenewable;
		int duedate;
		DataReader data = new DataReader(filename);
		try {
			
	    	nbTasks = data.next();
		    nbRenewable = data.next();
		    nbNonRenewable = data.next();
		    
		    int[] renewableResource = new int[nbRenewable];
		    int renewableAmount = 0;
		    int[] nonrenewableResource = new int[nbNonRenewable]; 
		    int nonrenewableAmount = 0;
		    
		    
		    for (int i = 0; i < nbRenewable; i++) {
				renewableResource[i] = data.next();
				renewableAmount += renewableResource[i];
			}
		    
		    for (int i = 0; i < nbNonRenewable; i++) {
				nonrenewableResource[i] = data.next();
				nonrenewableAmount += nonrenewableResource[i];
			}
		    
		    duedate = data.next();
		    
		    int totalResource = renewableAmount + nonrenewableAmount;
		    
		    int enterpriseNumber = 2*totalResource/(2*3);
		    generateEnterprise(2*duedate, 2*duedate, enterpriseNumber);
		    
		    
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
