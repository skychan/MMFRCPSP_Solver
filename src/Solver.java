import java.io.IOException;

public class Solver {

	public static void main(String[] args) {
		try {
//			SchedRCPSPMM.solve("data/test");
			RL.solve("data/test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Enterprise e = new Enterprise(500,600);
		e.setResourceAmount(5, 8);
		e.getResourceAmount(5);

	}

}
