import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
		    
		    int[] amtRenewableRes = new int[nbRenewable];
		    int amtRenewable = 0;
		    int[] amtNonRenewableRes = new int[nbNonRenewable]; 
		    int amtNonRenewable = 0;
		    
		    
		    for (int i = 0; i < nbRenewable; i++) {
				amtRenewableRes[i] = data.next();
				amtRenewable += amtRenewableRes[i];
			}
		    
		    for (int i = 0; i < nbNonRenewable; i++) {
				amtNonRenewableRes[i] = data.next();
				amtNonRenewable += amtNonRenewableRes[i];
			}
		    
		    duedate = data.next();
		    
		    int amtTotal = amtRenewable + amtNonRenewable;
		    
		    int overflow_factor = 2;
		    int[] nbEntPerRes = {1,6};		// {min,max}
		    int[] amtResPerEnt = {1,4};	    // {min,max}
		    
		    int[] costRes = {5,15};	// {min,max}
		    
		    int nbEnt = 4 * overflow_factor * amtTotal / (
		    		(nbEntPerRes[0] + nbEntPerRes[1]) * 
		    		(amtResPerEnt[0] + amtResPerEnt[1])
		    		);
		    
		    List<Integer> idxEnt = new ArrayList<Integer>();
		    for (int i = 0; i < nbEnt; i++) {
				idxEnt.add(i);
			}
		    
//		    System.out.println(idxEnt.toString());
		    
		    List<Enterprise> enterprise = generateEnterprise(2*duedate, 2*duedate, nbEnt);
		    int[] amtResource = combineArray(amtRenewableRes, amtNonRenewableRes);
 		    for (int type = 0; type < nbRenewable + nbNonRenewable; type++) {
				double nbSample = amtResource[type] * 2 /( 
						amtResPerEnt[0] + amtResPerEnt[1]
						);
//				System.out.println(nbSample);
				Collections.shuffle(idxEnt);
				
				for (int i = 0; i < nbSample; i++) {
					int index = idxEnt.get(i);
					Enterprise e = enterprise.get(index);
					int amount = overflow_factor * ThreadLocalRandom.current().nextInt(amtResPerEnt[0],amtResPerEnt[1] +1 );
					int cost = ThreadLocalRandom.current().nextInt(costRes[0],costRes[1] + 1);
					//					System.out.println(amount);
					e.setResourceAmount(type, amount);
					e.setResourceCost(type, cost);
				}
			}
		    
		    for (Enterprise e : enterprise) {
				System.out.println(e.toString());
			}
		    
		    double[] ratio = checkmount(enterprise, amtResource);
		    for (double d : ratio) {
				System.out.println(d);
			}
		    
		    Benchmark.Benchmark(enterprise, "src/output");
		    
//		    int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
		    
		    
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error: " + e);
		}
		
		 
	}
	
	public static int[] combineArray(int[] arr1, int[] arr2){
		int[] newArr = new int[arr1.length + arr2.length];
		for (int i = 0; i < arr1.length; i++) {
			newArr[i] = arr1[i];
		}
		for (int j = 0; j < arr2.length; j++) {
			newArr[arr1.length+j] = arr2[j];
		}		
		return newArr;
	}
	
	
	public static List<Enterprise> generateEnterprise(int width, int height, int amount) {
		List<Enterprise> eList = new ArrayList<Enterprise>();
		double[] quality = {1,10} ; // {min,max}
		for (int i = 0; i < amount; i++) {
			Enterprise e = new Enterprise(width, height);
			e.setIndex(i);
			e.setQuality(ThreadLocalRandom.current().nextDouble(quality[0],quality[1]));
			eList.add(e);
			
//			System.out.println(i + " : " + e.getX() + "," + e.getY());
//			e.getLocation();
		}
		return eList;
	}
	
	public static double[] checkmount(List<Enterprise> eList, int[] amtResource){
		int[] amtActual = new int[amtResource.length];
		for (Enterprise enterprise : eList) {
			for (int type = 0; type < amtResource.length; type++) {
				amtActual[type] += enterprise.getResourceAmount(type);
			}
		}
		
//		for (int i : amtActual) {
//			System.out.println(i);
//		}
		
		double[] ratio = new double[amtResource.length];
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] =  amtActual[i] / (amtResource[i] * 1.0);
		}
		return ratio;
	}
}
