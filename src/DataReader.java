import java.io.*;

public class DataReader {

        private StreamTokenizer st;

        public DataReader(String filename) throws IOException {
            FileInputStream fstream = new FileInputStream(filename);
            Reader r = new BufferedReader(new InputStreamReader(fstream));
            st = new StreamTokenizer(r);
        }

        public double next() throws IOException {

            st.nextToken();
            return st.nval;
        }
    
}
