import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ProviderGenerator {
	public static void main(String[] args) throws IOException {
		
		generate("data/test");
		
	}
	
	public static void generate(String filename) throws IOException {
		int nbTasks, nbRenewable, nbNonRenewable;
		int duedate;
		DataReader data = new DataReader(filename);
		try {
			
	    	nbTasks = (int) data.next();
		    nbRenewable = (int) data.next();
		    nbNonRenewable = (int) data.next();
		    
		    int[] amtRenewableRes = new int[nbRenewable];
		    int amtRenewable = 0;
		    int[] amtNonRenewableRes = new int[nbNonRenewable]; 
		    int amtNonRenewable = 0;
		    
		    
		    for (int i = 0; i < nbRenewable; i++) {
				amtRenewableRes[i] = (int) data.next();
				amtRenewable += amtRenewableRes[i];
			}
		    
		    for (int i = 0; i < nbNonRenewable; i++) {
				amtNonRenewableRes[i] = (int) data.next();
				amtNonRenewable += amtNonRenewableRes[i];
			}
		    
		    duedate = (int) data.next();
		    
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
		    /***
		     * set the random seed
		     */
		    Random generator = new Random(8);
		    
		    List<Enterprise> enterprise = generateEnterprise(2*duedate, 2*duedate, nbEnt, generator);
		    int[] amtResource = combineArray(amtRenewableRes, amtNonRenewableRes);
 		    for (int type = 0; type < nbRenewable + nbNonRenewable; type++) {
				double nbSample = amtResource[type] * 2 /( 
						amtResPerEnt[0] + amtResPerEnt[1]
						);
//				System.out.println(nbSample);
				Collections.shuffle(idxEnt, generator);;
				
				for (int i = 0; i < nbSample; i++) {
					int index = idxEnt.get(i);
					Enterprise e = enterprise.get(index);
					int amount = overflow_factor * (generator.nextInt(amtResPerEnt[1] + 1 - amtResPerEnt[0]) + amtResPerEnt[0]);
					int cost = generator.nextInt(costRes[1] + 1 - costRes[0]) + costRes[0];
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
	
	
	public static List<Enterprise> generateEnterprise(int width, int height, int amount, Random generator) {
		List<Enterprise> eList = new ArrayList<Enterprise>();
		double[] quality = {1,10} ; // {min,max}
		for (int i = 0; i < amount; i++) {
			double x = generator.nextDouble()*width;
			double y = generator.nextDouble()*height;
			Enterprise e = new Enterprise(x, y);
			e.setIndex(i);
			e.setQuality(generator.nextDouble()*(quality[1] - quality[0]) + quality[0]);
			eList.add(e);
			
//			System.out.println(i + " : " + e.getX() + "," + e.getY());
//			e.getLocation();
		}
		return eList;
	}
	
	public static double[] checkmount(List<Enterprise> eList, int[] amtResource){
		int[] amtActual = new int[amtResource.length];
		double[] actual = new double[amtResource.length];
		for (Enterprise enterprise : eList) {
			for (int type = 0; type < amtResource.length; type++) {
				int amount = enterprise.getResourceAmount().containsKey(type)?enterprise.getResourceAmount().get(type):0;
				amtActual[type] += amount;
			}
		}
		
//		for (int i : amtActual) {
//			System.out.println(i);
//		}
		
		double[] ratio = new double[amtResource.length];
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] =  amtActual[i] / (amtResource[i] * 1.0);
			actual[i] = (double) amtActual[i];
			
		}
		return actual;
	}
}
