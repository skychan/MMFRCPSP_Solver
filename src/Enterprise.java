import java.util.HashMap;
import java.util.Map;


public class Enterprise {
	/**
	 * Map range 
	 */
	private double width, height;
	
	/**
	 * Location r_j = ( x , y ) 
	 */
	private double x, y;
	
	private int index;
	/**
	 * Internal base attribute, base quality and base unit cost
	 */
//	private static double quality, cost;
	
	/**
	 * Use map to store resources
	 */
	private Map<Integer, Integer> resourceAmount = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> resourceCost = new HashMap<Integer, Integer>();
//	private Map<Integer, Double> resourceQualiyt = new HashMap<Integer, Double>();
	
	private double quality = 0;
	
	public Enterprise(double width, double height) {
		// initial the map range
		this.width = width;
		this.height = height;
		
		// generate location
		this.x = Math.random()*this.width;
		this.y = Math.random()*this.height;
		
		// generate quality globally
//		this.quality = 10*Math.random() + 10;
//		
//		// generate cost globally
//		this.cost = 10*Math.random() + 10;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public void setResourceAmount(int type, int amount){
		this.resourceAmount.put(type, amount);
	}	
	
	public int getResourceAmount(int type){
		int amount;
		if (this.resourceAmount.containsKey(type)) {
			amount = (int) this.resourceAmount.get(type);
		} else {
			amount = 0;
		}
		return amount;
	}

	public Map<Integer, Integer> getResourceCost() {
		return resourceCost;
	}

	public void setResourceCost(int type, int cost) {
		this.resourceCost.put(type, cost);
	}

//	public Map<Integer, Double> getResourceQualiyt() {
//		return resourceQualiyt;
//	}
//
//	public void setResourceQualiyt(int type, double quality) {
//		this.resourceQualiyt.put(type,quality);		
//	}
	
	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}
	
	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		
		return "Enterprise " + index + ": location: (" + x + "," + y + ")\n" 
				+ "   resourceAmount=" + resourceAmount.toString() + "\n"
				+ "   resourceCost=" + resourceCost.toString() + "\n" 
				+ "   resourceQualiyt=" + quality + "\n";
	}


	
	

}
