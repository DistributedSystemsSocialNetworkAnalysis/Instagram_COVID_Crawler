import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class DataElaborator {
	
	public static void deleteCopies(File data) throws IOException, ParseException {
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
	
	@SuppressWarnings("unchecked")
	public static void filterInformations(File f) throws IOException {
		System.out.println("Filtraggio...");
		Reader reader = null;
		JSONArray posts = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
		    JSONParser parser = new JSONParser();
		    posts = (JSONArray) parser.parse(reader);	
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		File fileFiltered = new File(f.getAbsolutePath() + " - filtered");
		if(!fileFiltered.exists()) 
			fileFiltered.createNewFile();
		JSONArray filteredPosts = new JSONArray();
		
		for(int i = 0; i < posts.size(); i++) {		
			JSONObject newPost = new JSONObject(); // nuovo post con info filtrate
			JSONObject post = (JSONObject) posts.get(i); // post originale
			
			newPost.put("AccessibilityCaption", ((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("accessibility_caption"));
	    	JSONArray captionText = ((JSONArray)((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("edge_media_to_caption")).get("edges"));
	    	
	    	if(captionText.size()!=0)
	    		newPost.put("CaptionText",((JSONObject)((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("edge_media_to_caption")).get("edges")).get(0)).get("node")).get("text"));
	    	else newPost.put("CaptionText",null);
	    		
	    	newPost.put("NumberOfLikes",((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("edge_media_preview_like")).get("count"));
	    	
	    	newPost.put("NumberOfComments",((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("edge_media_to_parent_comment")).get("count"));
	    	
	    	newPost.put("Timestamp",((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("taken_at_timestamp"));
	    	long timestamp = (long) ((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("taken_at_timestamp");	 
	    	Timestamp t = new Timestamp(timestamp*1000);
	    	Date date = new Date(t.getTime());
	    	newPost.put("LocalDate", "" + date);
	    	
	    	JSONObject location = (JSONObject) ((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("location");
	    	if(location!=null && location.containsKey("name"))
	        	newPost.put("Location",((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("location")).get("name"));
	        else 
	        	newPost.put("Location",null);
	    	
	    	filteredPosts.add(newPost);
		}
		
		/* scrivo il file con i post filtrati */
		Writer writer = null;	
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFiltered), "utf-8"));
		    writer.write(filteredPosts.toJSONString());
		    writer.flush();
		} catch (IOException ex) {
		    // Report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		
		System.out.println("Filtraggio terminato.");
	}
	
	public static void elaborate() throws IOException, ParseException {
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
							
							//deleteCopies(files[j]);
							filterInformations(files[j]);
							
							JSONArray posts = null;
;							try {
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
		
		System.out.println("\n----- STATISTICHE: -----");
		System.out.println("Numero di post: " + numOfPost);
		System.out.println("Numero di file: " + numOfFile);
		System.out.println("Non parsabili: " + notParsable);
	}

	
	public static void main(String[] args) throws IOException, ParseException {
		elaborate();
	}	
}

