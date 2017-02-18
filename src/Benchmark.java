import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate the benchmark of enterprise, the file goes like:
 * #Entprise
 * (x,y) Quality #type
 * type1 amount cost | type2 amount ...
 * ....
 * 
 * @author CSK
 *
 */
public class Benchmark {

	public static void Benchmark(List<Enterprise> eList, String outputFile) {
		// TODO Auto-generated constructor stub
		List<String> outputList = new ArrayList<String>();
		Path filename = Paths.get(outputFile);
		int nbEnt = eList.size();
		outputList.add(Integer.toString(nbEnt));
		for (Enterprise e : eList) {
			String index = Integer.toString(e.getIndex());
			String location = Double.toString(e.getX()) + " " + Double.toString(e.getY());
			String quality = Double.toString(e.getQuality());
			String nbType = Integer.toString(e.getResourceCost().size());
			outputList.add(index + " " + location + " " + quality + " " + nbType);
			String data = "";
//			e.getResourceCost().keySet()
			
			for (int key : e.getResourceCost().keySet()) {
				data += Integer.toString(key) + " ";
				data += Integer.toString(e.getResourceAmount().get(key)) + " ";
				data += Integer.toString(e.getResourceCost().get(key)) + " ";
			}
			outputList.add(data);
		}
		
		try {        	     	
			Files.write(filename,outputList,Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
