import java.io.IOException;

public class Solver {

	public static void main(String[] args) {
		try {
			SchedRCPSPMM.solve(args);
//			RL.solve(args);
//			Verify.solve("data/test2");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
