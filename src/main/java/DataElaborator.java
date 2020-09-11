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
	    	
	    	if(captionText.size()!=0) {
	    		String text = (String) ((JSONObject)((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("edge_media_to_caption")).get("edges")).get(0)).get("node")).get("text");
	    		newPost.put("CaptionText",text);
	    		JSONArray hashtags = getRelatedHashtags(text); // parsing degli hashtag a partire dalla caption del post
	    		newPost.put("Hashtags", hashtags);
	    	}
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
	
	
	public static void countOccurrences() throws IOException {
		File data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
		HashMap<String,Integer> occurrences = new HashMap<String,Integer>();
		
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
						/* scorro i singoli file */
						for(int j=0; j<files.length; j++) {
							
							if(files[j].getName().contains("filtered")) {
								/* parso il file */
								Reader reader = null;
								JSONArray posts = null;
								try {
								    reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[j]), "utf-8"));
								    JSONParser parser = new JSONParser();
								    posts = (JSONArray) parser.parse(reader);	
								} catch (Exception ex) {
									ex.printStackTrace();
								}
														
								for(int m=0; m<posts.size(); m++) {
									JSONArray hashtags = (JSONArray) ((JSONObject)posts.get(m)).get("Hashtags");
									if(hashtags!=null) {
										for(int n=0; n<hashtags.size(); n++) {
											if(occurrences.containsKey(hashtags.get(n))) {
												occurrences.put( (String)hashtags.get(n), new Integer( occurrences.get((String)hashtags.get(n))+1 ) ) ;
											} else occurrences.put((String)hashtags.get(n), new Integer(1));						
										}
									}
								}
							}
							
						}
						
					}
				}
			}
		}
		
		File hashtagStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_statistics.txt");
		if(!hashtagStatistics.exists())
			hashtagStatistics.createNewFile();
		
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hashtagStatistics), "utf-8"));
		    writer.flush();
		} catch (IOException ex) {
		    // Report
		}
		
		writer.write("HASHTAG STATISTICS:\r\n");
		writer.append("\r\n");
		
		//writer.append("+-------------------------------------+----------------+-------------+\n");
	    //writer.append("|        Hashtag                      |      Number of occurrences   |\n");
	    //writer.append("+-------------------------------------+----------------+-------------+\n");

		
	    
	    for(String name: occurrences.keySet()) {
	    	
	    }
	    
		for (String name: occurrences.keySet()){
            String key =name.toString();
            int value = ((Integer)occurrences.get(name)).intValue();
            //writer.append(String.format("%s                            %s            \r\n", key, value+""));
            writer.append(String.format("%s       %s\r\n", key, value + ""));
            System.out.println(key + " " + value);  
		}
		
		writer.append("+--------------------------------------------------------+\n");
	}

	
	public static void elaborate() throws IOException, ParseException {
		File data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
		int numOfFile = 0, numOfPost = 0, notParsable = 0;
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
							if(!files[j].getName().contains("filtered"))
								filterInformations(files[j]);
							
							int pars = isParsable(files[j]);
							if(pars == -1) {
								notParsable ++;
								parsable = false;
							}
													
							if(parsable)
								numOfPost = numOfPost + pars;
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
	
	/*
	public static JSONArray getRelatedHashtags(String caption) {
		if(caption!=null && caption.contains("#")) {
			HashSet<String> hashtags = new HashSet<String>();
			int occ = caption.indexOf("#");
			String cleanCaption = caption.substring(occ, caption.length());
			String[] listOfHashtags = cleanCaption.split("#");
			for(int i=0; i<listOfHashtags.length; i++) {				
				if(!listOfHashtags[i].equals("")) {
					String hashtag = listOfHashtags[i].substring(0, listOfHashtags[i].length()-1);
					hashtag.replaceAll("\\s+","");
					hashtags.add(hashtag);
				}
			}
			
			JSONArray output = new JSONArray();	

			for(String s: hashtags) 
				output.add(s);
						
			return output;
		}
		return null;
	} */
	
	@SuppressWarnings("unchecked")
	public static JSONArray getRelatedHashtags(String caption) {
		if(caption!=null && caption.contains("#")) {
			JSONArray hashtags = new JSONArray();
			int occ = caption.indexOf("#");
			String cleanCaption = caption.substring(occ, caption.length());
			
			int i = 0;
			while(i < cleanCaption.length()) {
				if(cleanCaption.charAt(i)=='#') {
					int j = i+1;
					while(j<cleanCaption.length() && !Character.isWhitespace(cleanCaption.charAt(j))) {
						if(cleanCaption.charAt(j)=='#' & j!=i) {
							j--;
							break;
						}
							
						j++;
					}
					
					String hashtag = cleanCaption.substring(i, j);
					if(hashtag.length()>1)
						hashtags.add(hashtag);
					System.out.println("Ho aggiunto " + hashtag);
				}
				
				i++;
			}			
			return hashtags;
		}		
		return null;
	}

	
	public static int isParsable(File f) throws IOException {
		JSONArray posts = null;
		try {
			Reader reader = null;
			try {
			    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
			    JSONParser parser = new JSONParser();
			    posts = (JSONArray) parser.parse(reader);								    
			} catch (IOException ex) {
			    // Report
			} finally {
			   try {reader.close();} catch (Exception ex) {/*ignore*/}
			}

		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("Errore: file " + f + " non parsabile.");
			
			/* sposto il file non parsabile sul desktop per una successiva revisione */
			Path temp = Files.move(Paths.get(f.getAbsolutePath()), Paths.get("C:\\Users\\marti\\Desktop\\" + f.getName())); 	  
			if(temp != null) 
				System.out.println("File renamed and moved successfully"); 
			else System.out.println("Failed to move the file"); 
			
			return -1;
		}
		
		return posts.size();
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		//elaborate();
		
		countOccurrences();
	}	
}

