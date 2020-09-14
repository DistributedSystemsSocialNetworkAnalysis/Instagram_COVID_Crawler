import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class DataElaborator {
	static File hashtagStatistics;
	static File locationStatistics;
	static File accessibilityStatistics;
	static File data;
	
	
	public DataElaborator() throws IOException {
		hashtagStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_statistics.txt");
		locationStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\locations_statistics.txt");
		accessibilityStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\accessibility_statistics.txt");
		data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
		
		if(!hashtagStatistics.exists())
			hashtagStatistics.createNewFile();
		
		if(!locationStatistics.exists())
			locationStatistics.createNewFile();
		
		if(!accessibilityStatistics.exists())
			accessibilityStatistics.createNewFile();
	}
	
	
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
	
	
	public void countOccurrences() throws IOException {
		HashMap<String,Integer> hashtagOccurrences = new HashMap<String,Integer>();
		HashMap<String,Integer> locationOccurrences = new HashMap<String,Integer>();
		
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
									String location = (String) ((JSONObject)posts.get(m)).get("Location");
									
									if(location!=null) {
										if(locationOccurrences.containsKey(location)) {
											locationOccurrences.put(location, new Integer(locationOccurrences.get(location)+1)) ;
										} 
										else locationOccurrences.put(location, new Integer(1));	
									}
									
									if(hashtags!=null) {
										for(int n=0; n<hashtags.size(); n++) {
											if(hashtagOccurrences.containsKey(hashtags.get(n))) {
												hashtagOccurrences.put( (String)hashtags.get(n), new Integer( hashtagOccurrences.get((String)hashtags.get(n))+1 ) ) ;
											} 
											else hashtagOccurrences.put((String)hashtags.get(n), new Integer(1));						
										}
									}
								}
							}
							
						}
						
					}
				}
			}
		}
		
		Writer writer1 = null, writer2 = null;
		try {
		    writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hashtagStatistics), "utf-8"));
		    writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(locationStatistics), "utf-8"));
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		
		writer1.write("HASHTAG STATISTICS:\r\n");
		writer1.append("\r\n");	    
		HashMap<String,Integer> map1 = (HashMap<String, Integer>) HashMapSorting.sortByValue(hashtagOccurrences);
		for (String name: map1.keySet()){
            String key = name.toString();
            int value = ((Integer)map1.get(name)).intValue();
            writer1.append(String.format("%s       %s\r\n", key, value + ""));
            System.out.println(key + " " + value);  
		}

		
		writer2.write("LOCATION STATISTICS:\r\n");
		writer2.append("\r\n");
		HashMap<String,Integer> map2 = (HashMap<String, Integer>) HashMapSorting.sortByValue(locationOccurrences);
		for (String loc: map2.keySet()){
            String key = loc.toString();
            int value = ((Integer)map2.get(loc)).intValue();
            writer2.append(String.format("%s       %s\r\n", key, value + ""));
            System.out.println(key + " " + value);  
		}
	}

	
	public static void elaborate() throws IOException, ParseException {
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
	
	
	public void getAccessibilityCaptions() throws IOException {
		ArrayList<String> captions = new ArrayList<String>();
		//ArrayList<String> captionsNonAscii = new ArrayList<String>();
		int photos = 0, videos = 0, people = 0, text = 0, out = 0, in = 0, total = 0;
		//int notascii = 0;
		
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
									total++;
									String caption = (String) ((JSONObject)posts.get(m)).get("AccessibilityCaption");
									
									if(caption!= null) {
										/*
										if(!caption.matches("\\A\\p{ASCII}*\\z")) {
											notascii++;
											System.out.println("NON ASCII: " + caption);
											captionsNonAscii.add(caption);
											break;
										} */
										if(caption.contains("Photo"))
											photos++;
										if(caption.contains("Video"))
											videos++; 
										if(caption.contains("people") || caption.contains("person"))
											people++;
										if(caption.contains("text")) {
											/*
											if(caption.contains("text that says")) {
												int index = caption.indexOf("says");
												String s = caption.substring(index+5,caption.length());
												System.out.println("TESTO: " + s);
												captions.add(s);
											} */
											text++; 
										}
											
										if(caption.contains("indoor"))
											in++;										
										if(caption.contains("outdoor"))
											out++;
										
										if(caption.contains("Image may contain:")) {
											String[] s = caption.split(":");
											String cleanCaption;
											if(s[1].length()!=0) {
												cleanCaption = s[1].substring(1, s[1].length());
												captions.add(cleanCaption);
											} else captions.add(caption);
										} else captions.add(caption);
									} 
								}
								
								
							}
							
						}
					}
				}
			}
		}
		
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accessibilityStatistics), "utf-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		writer.write("---- ACCESSIBILITY STATISTICS: ----\r\n");
		writer.append("\r\n");
		writer.append("Number of media (total): " + total + "\r\n");
		writer.append("Number of media with accessibility caption: " + captions.size() + "\r\n");		
		writer.append("Number of media with people: " + people + "\r\n");
		writer.append("Number of media with text: " + text + "\r\n");
		writer.append("Number of media taken outdoor: " + out + "\r\n");
		writer.append("Number of media taken indoor: " + in + "\r\n");
		writer.append("Number of photos: " + photos + "\r\n");
		writer.append("Number of videos: " + videos + "\r\n");
		//writer.append("Number of captions not ASCII: " + notascii + "\r\n");
		writer.append("\r\n");
		
		for(String c: captions) {
			try {
				writer.append(c + "\r\n");
				writer.flush();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		/*
		writer.append("\r\n");
		writer.append("[NOT ASCII CAPTIONS:]\r\n");
		for(String c: captionsNonAscii) {
			try {
				writer.append(c + "\r\n");
				writer.flush();
				//System.out.println(c);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} */
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
		DataElaborator elab = new DataElaborator();
		//elab.countOccurrences(); // statistiche su luoghi e hashtags
		
		elab.getAccessibilityCaptions();
	}	
}

