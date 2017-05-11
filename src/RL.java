import ilog.concert.*;
import ilog.cp.*;

import java.io.*;
import java.util.*;

public class RL {
//	
//	static class IntervalVarList extends ArrayList<IloIntervalVar> {
//        public IloIntervalVar[] toArray() {
//            return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this.size()]);
//        }
//    }
//
//    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
//        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
//    }
	
	public static void solve(String[] args) throws IOException {
		int failLimit = 30000;
		int nbTasks, nbRenewable, nbNonRenewable;
		int duedate;
		int nbEnterprise;
		
		List<Enterprise> enterprises = new ArrayList<Enterprise>();
	    
		String projectFileName = "data/test";
	    String enterpriseFileName = "src/output";
	        
	    if (args.length > 1){
	    	projectFileName = args[0];
	        enterpriseFileName = args[1];
	        }
	    if (args.length > 2)
	    	failLimit = Integer.parseInt(args[2]);
	    	    
	    IloCP cp = new IloCP();
	    
	    DataReader entData = new DataReader(enterpriseFileName);
	    
	    try {
	    	nbEnterprise =  entData.next();
			for (int i = 0; i < nbEnterprise; i++) {
				int idx =  entData.next();
				int x = entData.next();
				int y = entData.next();
				int quality = entData.next();
				Enterprise e = new Enterprise(x, y);
				e.setIndex(idx);
				e.setQuality(quality);
			
				int nbType =  entData.next();
				
				for (int j = 0; j < nbType; j++) {
					int type =  entData.next();
					int amount =  entData.next();
					int cost =  entData.next();
					e.setResourceAmount(type, amount);
					e.setResourceCost(type, cost);
				}
				enterprises.add(e);
			}
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println("Error: " + e);
		}
	    
	    DataReader data = new DataReader(projectFileName);
	    
	    try {
			nbTasks = data.next();
			nbRenewable = data.next();
			nbNonRenewable = data.next();
			
			int[] nbModes = new int[nbTasks];
			
			int oldAmount;
			for (int type = 0; type < nbRenewable; type++) {
				oldAmount = data.next();
			}
			for (int type = 0; type < nbNonRenewable; type++) {
				oldAmount = data.next();
			}
			
			duedate = data.next();
			
			for (int i = 0; i < nbTasks; i++) {
				int taskId = data.next();
				nbModes[i] = data.next();
				int nbSucc = data.next();
				
				for (int s = 0; s < nbSucc; s++) {
					int succId = data.next();
				}
			}
			
			for (int i = 0; i < nbTasks; i++) {
				int taskId = data.next();
				for (int k = 0; k < nbModes[i]; k++) {
					int modeId = data.next();
					int duration = data.next();
					
					int q;
					for (int type = 0; type < nbRenewable; type++) {
						q = data.next();
					}
					for (int type = 0; type < nbNonRenewable; type++) {
						q = data.next();
					}
				}
			}
			
			
			// constraints include: 
			// precedence
			// resource amount 
			
			
		} catch (IOException e) {
			// TODO: handle exception
			System.out.println("Error: " + e);
		}
	}
}
