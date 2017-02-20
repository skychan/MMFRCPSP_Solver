import java.util.Arrays;

import ilog.concert.IloNumExpr;


public class Location {
	private IloNumExpr[] r = new IloNumExpr[2];
	private int id;

		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public IloNumExpr[] getR() {
		return r;
	}
	public void setR(IloNumExpr[] r) {
		this.r = r;
	}
	@Override
	public String toString() {
		return "Location [" + Arrays.toString(r) + "]";
	}
}
