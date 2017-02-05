import ilog.concert.*;
import ilog.cp.*;
import java.io.*;
import java.util.*;

public class RL {
	
	static class IntervalVarList extends ArrayList<IloIntervalVar> {
        public IloIntervalVar[] toArray() {
            return (IloIntervalVar[]) this.toArray(new IloIntervalVar[this.size()]);
        }
    }

    static IloIntExpr[] arrayFromList(List<IloIntExpr> list) {
        return (IloIntExpr[]) list.toArray(new IloIntExpr[list.size()]);
    }
	
	public static void solve(String filename) throws IOException {
//		System.out.println(filename);
		int nbTasks, nbRenewable, nbNonRenewable;
		DataReader data = new DataReader(filename);
		
		 nbTasks = data.next();
         nbRenewable = data.next();
         nbNonRenewable = data.next();
		
	}
}
