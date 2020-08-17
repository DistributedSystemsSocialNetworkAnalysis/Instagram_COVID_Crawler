import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DuplicateEliminator {
	static File data;
	
	public DuplicateEliminator(File f) {
		data = f;
	}
	
	public void deleteCopies() throws IOException, ParseException {
		FileReader fr = new FileReader(data);
		JSONParser parser = new JSONParser();
		JSONArray posts = (JSONArray) parser.parse(fr);
		int length = posts.size();
		
		System.out.println("Dimensione pre-filtraggio: " + length);
				
		Set<JSONObject> set = new HashSet<JSONObject>();

		for(int i = 0; i < length; i++){
		  set.add((JSONObject) posts.get(i));
		}
		
		System.out.println("Dimensione dopo il filtraggio: " + set.size());
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		File f = new File("C:\\Users\\marti\\git\\TirocinioProtano\\#covid_19_hashtag_data.txt");
		DuplicateEliminator de = new DuplicateEliminator(f);
		
		de.deleteCopies();
	}
}

