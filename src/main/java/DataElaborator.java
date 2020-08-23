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

public class DataElaborator {
	static File data;
	
	public DataElaborator(File f) {
		data = f;
	}
	
	public void deleteCopies() throws IOException, ParseException {
		/*
		FileReader fr = new FileReader(data);
		JSONParser parser = new JSONParser();
		JSONArray posts = (JSONArray) parser.parse(fr);
		int length = posts.size();
		
		System.out.println("Dimensione pre-filtraggio: " + length);
				
		Set<JSONObject> set = new HashSet<JSONObject>();

		for(int i = 0; i < length; i++){
		  set.add((JSONObject) posts.get(i));
		}
		*/
		
		FileReader fr = new FileReader(data);
		JSONParser parser = new JSONParser();
		JSONArray posts = (JSONArray) parser.parse(fr);
		int length = posts.size();
		
		System.out.println("Dimensione pre-filtraggio: " + length);
		
		Set<String> set = new HashSet<String>();
		for(int i = 0; i < length; i++){
			 set.add(((JSONObject)posts.get(i)).toJSONString());
		}
		
		System.out.println("Dimensione dopo il filtraggio: " + set.size());
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		File data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
		int numOfFile = 0;
		int numOfPost = 0;
		int notParsable = 0;
		boolean parsable = true;
		
		if(data.isDirectory()) {
			File[] directories = data.listFiles();
			
			/* scorre le cartelle con date */
			for(int i=0; i < directories.length; i++) {
				
				if(directories[i].isDirectory()) {
					File[] f = directories[i].listFiles();
					
					/* scorro le cartelle degli hashtag*/
					for(int k=0; k < f.length; k++) {
						
						File[] files = f[i].listFiles();
						numOfFile = numOfFile + files.length;
						
						for(int j=0; j<files.length; j++) {
							parsable = true; 
							
							JSONArray posts = null;
							try {
								FileReader fr = new FileReader(files[j]);
								JSONParser parser = new JSONParser();
								posts = (JSONArray) parser.parse(fr);
							} catch(Exception e) {
								System.err.println("Errore: file " + files[j] + " non parsabile.");
								parsable = false;
								notParsable++;
							}
							
							if(parsable)
								numOfPost = numOfPost + posts.size();
						}
					}
				}
			}
		}
		
		System.out.println("Numero di post: " + numOfPost);
		System.out.println("Numero di file: " + numOfFile);
		System.out.println("Non parsabili: " + notParsable);
	}
	
	
}

