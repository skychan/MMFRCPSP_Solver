import java.util.HashMap;
import java.util.Map;


public class Enterprise {
	/**
	 * Map range 
	 */
	private static double width, height;
	
	/**
	 * Location r_j = ( x , y ) 
	 */
	private static double x, y;
	
	/**
	 * Internal base attribute, base quality and base unit cost
	 */
	private static double quality, cost;
	
	/**
	 * Use map to store resources
	 */
	private static Map resources = new HashMap();
	
	public Enterprise(double width, double height) {
		// initial the map range
		this.width = width;
		this.height = height;
		
		// generate location
		this.x = Math.random()*this.width;
		this.y = Math.random()*this.height;
	}
	
	public void addResource(int type, int amount){
		this.resources.put(type, amount);
	}
	
	public void getResource(int type){
		int amount;
		if (this.resources.containsKey(type)) {
			amount = (int) this.resources.get(type);
		} else {
			amount = 0;

		}
		System.out.println(amount);
	}
	
	public void getLocation(){
		System.out.format("x = %f, y = %f\n", this.x, this.y);
	}


}
