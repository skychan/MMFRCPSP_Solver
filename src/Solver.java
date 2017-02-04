import java.io.IOException;

public class Solver {

	public static void main(String[] args) {
		try {
			SchedRCPSPMM.solve("data/m12_5.mm");
//			RL.solve("data/m12_5.mm");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
