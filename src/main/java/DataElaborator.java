import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DataElaborator {
	static File data;
	
	public void deleteCopies() throws IOException, ParseException {
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
			
			/* scorre le cartelle con date (giorno-mese-anno) */
			for(int i=0; i < directories.length; i++) {
				System.out.println("Entro nella cartella: " + directories[i].getName());
				
				if(directories[i].isDirectory()) {
					File[] f = directories[i].listFiles();
					
					/* scorro le cartelle degli hashtag*/
					for(int k=0; k < f.length; k++) {
						System.out.println("Entro nella cartella: " + f[k].getName());
						
						File[] files = f[k].listFiles();
						numOfFile = numOfFile + files.length;
						
						/* scorro i singoli file */
						for(int j=0; j<files.length; j++) {
							System.out.println(files[j]);
							parsable = true; 
							
							JSONArray posts = null;
							try {
								Reader reader = null;
								try {
								    reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[j]), "utf-8"));
								    JSONParser parser = new JSONParser();
								    posts = (JSONArray) parser.parse(reader);								    						    
								} catch (IOException ex) {
								    // Report
								} finally {
								   try {reader.close();} catch (Exception ex) {/*ignore*/}
								}
				
							} catch(Exception e) {
								e.printStackTrace();
								System.err.println("Errore: file " + files[j] + " non parsabile.");
								
								/* sposto il file non parsabile sul desktop per una successiva revisione */
								Path temp = Files.move(Paths.get(files[j].getAbsolutePath()), Paths.get("C:\\Users\\marti\\Desktop\\" + files[j].getName())); 
								  
								if(temp != null) 
									System.out.println("File renamed and moved successfully"); 
								else System.out.println("Failed to move the file"); 
								
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

