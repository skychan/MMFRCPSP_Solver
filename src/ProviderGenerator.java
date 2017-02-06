import java.io.IOException;

public class ProviderGenerator {
	public static void main(String[] args) {
		
	}
	
	public static void generate(String filename) throws IOException {
		int nbTasks, nbRenewable, nbNonRenewable;
		DataReader data = new DataReader(filename);
		try {
			
	    	nbTasks = data.next();
		    nbRenewable = data.next();
		    nbNonRenewable = data.next();
		    
		    
		    
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error: " + e);
		}
		
		 
	}
	
	public static void generatemap() {
		System.out.println("1");
//		return ;
	}
}
