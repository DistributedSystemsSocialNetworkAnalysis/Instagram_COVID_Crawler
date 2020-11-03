import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.util.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.*;
import org.json.simple.parser.*;

import weka.core.Stopwords;


public class DataElaborator {
	static File hashtagStatistics;
	static File hashtagCSV;
	static File locationStatistics;
	static File accessibilityStatistics;
	static File accessibilityTokens;
	static File userStatistics;
	static File data;
	static ArrayList<String> fixedHashtags = new ArrayList<String>(Arrays.asList(new String[] {"#coronavirus","#covid","#covid19","#covid_19","#quarantine","#quarantena",
			"#quarantinelife","#lockdown","#lockdowndiaries","#stayhome","#socialdistance","#socialdistancing","#pandemic","#pandemic2020", "#andràtuttobene"}));
	static Stopwords sw;

	public DataElaborator() throws IOException {
		hashtagStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_statistics.txt");
		locationStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\locations_statistics.txt");
		accessibilityStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\accessibility_statistics.txt");
		accessibilityTokens = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\accessibility_tokens.txt");
		userStatistics = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\user_statistics.txt");
		data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");

		if(!hashtagStatistics.exists())
			hashtagStatistics.createNewFile();

		if(!locationStatistics.exists())
			locationStatistics.createNewFile();

		if(!accessibilityStatistics.exists())
			accessibilityStatistics.createNewFile();

		if(!userStatistics.exists())
			userStatistics.createNewFile();
		
		sw = setStopwords();
	}

	
	@SuppressWarnings("deprecation")
	public void count() throws IOException {
		HashSet<Long> id = new HashSet<Long>();
		HashMap<String,Integer> ownerOccurrences = new HashMap<String,Integer>();
		long max = 1598423895, min = 1598423895;
		int c = 0;
		String name = null;
		int num = 0; 
		String[] set = new String[1063];
		String [] date = new String[1063];
		int r = 0;
		
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
							if(!files[j].getName().contains("filtered")) {
								Reader reader = null;
								JSONArray posts = null;
								try {
									reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[j]), "utf-8"));
									JSONParser parser = new JSONParser();
									posts = (JSONArray) parser.parse(reader);	
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								
								
								for(int p=0; p<posts.size(); p++) {
									JSONObject post = (JSONObject) posts.get(p);
									String owner = (String) ((JSONObject)((JSONObject)((JSONObject)post.get("graphql")).get("shortcode_media")).get("owner")).get("username");
									JSONObject temp = (JSONObject) (((JSONObject)post.get("graphql"))).get("shortcode_media");
									String id1 = null; 
									if(temp.containsKey("id")) {
										id1 = (String) ((JSONObject)((JSONObject)( (JSONObject)post.get("graphql"))).get("shortcode_media")).get("id");
										Long id2 = Long.parseLong(id1);
										if(!id.contains(id2)) {
											if(ownerOccurrences.containsKey(owner)) {
												ownerOccurrences.put(owner, new Integer(ownerOccurrences.get(owner)+1)) ;
											} 
											else ownerOccurrences.put(owner, new Integer(1));
										}
										
										id.add(id2);
									}
									long timestamp = (long) ((JSONObject)((JSONObject)((JSONObject)post.get("graphql"))).get("shortcode_media")).get("taken_at_timestamp");	 
									Timestamp t = new Timestamp(timestamp*1000);
									Date d = new Date(t.getTime());
									//System.out.println(d.toString());
									
									if(!d.toString().contains("2020") && !d.toString().contains("2019")) {
										name = files[j].getName();
										set[r] = name;
										date[r] = d.toString();
										r++;
										c++;
									} else {
											//System.out.println(d);
										if(timestamp < min) min = timestamp;
										if(max < timestamp) max = timestamp;	
											
										//} else c++;
									
									} 
							
							}
							
						}
					}
				}
			}
		}
		}
		
		Timestamp tmax = new Timestamp(max*1000);
		Timestamp tmin = new Timestamp(min*1000);
		Date datemax = new Date(tmax.getTime());
		Date datemin = new Date(tmin.getTime());
		
		/*	
		HashMap<String,Integer> map3 = (HashMap<String, Integer>) HashMapSorting.sortByValue(ownerOccurrences);
		TableFormatter formatter3 = new TableFormatter("users", new String[] {"posts", "username"});
		formatter3.fillTable1(map3); */
		
		System.out.println("DATA MINIMA: " + datemin);
		System.out.println("DATA MASSIMA: " + datemax);
		System.out.println("Numero post: " + id.size());
		System.out.println("Esclusi: " + c);
		for(int w=0; w<set.length;w++) {
			System.out.println(set[w]);
			System.out.println(date[w]);
			System.out.println();
		}
		
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

			newPost.put("Owner", ((JSONObject)((JSONObject)((JSONObject)post.get("graphql")).get("shortcode_media")).get("owner")).get("username"));

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
		HashMap<String,Integer> ownerOccurrences = new HashMap<String,Integer>();
		HashMap<String,Integer> tokenOccurrences = new HashMap<String,Integer>();
		ArrayList<String> fixedHashtags = new ArrayList<String>(Arrays.asList(new String[] {"#coronavirus","#covid","#covid19","#covid_19","#quarantine","#quarantena",
				"#quarantinelife","#lockdown","#lockdowndiaries","#stayhome","#socialdistance","#socialdistancing","#pandemic","#pandemic2020", "#andràtuttobene"}));

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

							/* considero solo i file filtrati */
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
									String owner = (String) ((JSONObject)posts.get(m)).get("Owner");
									String captionText = (String) ((JSONObject)posts.get(m)).get("CaptionText");

									if(captionText!=null) {

										StringTokenizer tokenizer = new StringTokenizer(captionText);
										while(tokenizer.hasMoreTokens()) {
											String s = tokenizer.nextToken();
											if(!s.contains("http") && !s.contains("%") && !s.contains("?") && !s.contains("#") && !s.contains("@") && s.matches("\\A\\p{ASCII}*\\z") && !sw.is(s) && !isNumber(s)) {
												if(s.contains(",")) s = s.replace(",", "");
												if(s.contains(".")) s = s.replace(".", "");
												if(s.contains("(")) s = s.replace("(", "");
												if(s.contains(")")) s = s.replace(")", "");
												if(s.contains(":")) s = s.replace(":", "");
												if(s.contains(";")) s = s.replace(";", "");
												if(s.contains("!")) s = s.replace("!", "");
												if(s.contains(" ")) s = s.replace(" ", "");
												s = s.toLowerCase();
												if(tokenOccurrences.containsKey(s))
													tokenOccurrences.put(s, new Integer(tokenOccurrences.get(s)+1));
												else tokenOccurrences.put(s, new Integer(1));
											}
										}
									}

									if(location!=null) { 
										
										if(location.contains(",")) {
											if(location.contains(" ")) location = location.replace(" ", "");
											String[] s = location.split(",");
											location = s[s.length-1];
										}
										
										if(location.matches("\\A\\p{ASCII}*\\z")) {
											if(locationOccurrences.containsKey(location)) {
												locationOccurrences.put(location, new Integer(locationOccurrences.get(location)+1));
											} else locationOccurrences.put(location, new Integer(1));
										}
									}

									if(hashtags!=null) {
										for(int n=0; n<hashtags.size(); n++) {
											if(!fixedHashtags.contains(hashtags.get(n))) {
												if(hashtagOccurrences.containsKey(hashtags.get(n))) {
													hashtagOccurrences.put( (String)hashtags.get(n), new Integer( hashtagOccurrences.get((String)hashtags.get(n))+1 ) ) ;
												} 
												else hashtagOccurrences.put((String)hashtags.get(n), new Integer(1));
											}
										}
									}

									if(ownerOccurrences.containsKey(owner)) {
										ownerOccurrences.put(owner, new Integer(ownerOccurrences.get(owner)+1)) ;
									} 
									else ownerOccurrences.put(owner, new Integer(1));	

								}
							}

						}

					}
				}
			}
		}

		Writer writer1 = null, writer2 = null, writer3 = null;
		try {
			writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hashtagStatistics), "utf-8"));
			writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(locationStatistics), "utf-8"));
			writer3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userStatistics), "utf-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}


		writer1.write("HASHTAG STATISTICS:\r\n");
		writer1.append("\r\n");	    

		HashMap<String,Integer> map1 = (HashMap<String, Integer>) HashMapSorting.sortByValue(hashtagOccurrences);
		writer1.append("Number of different hashtags: " + map1.keySet().size() + "\r\n");
		String[] stat = getHashtagsStatistics();
		writer1.append("Min of hashtags: " + stat[0] + "\r\n");
		writer1.append("Average of hashtags: " + stat[1] + "\r\n");
		writer1.append("Max of hashtags: " + stat[2] + "\r\n");

		writer1.append("\r\n");	

		TableFormatter formatter1 = new TableFormatter("hashtags", new String[] {"weight", "word"});
		formatter1.fillTable1(map1);
		for (String name: map1.keySet()){
			String key = name.toString(); 
			int value = ((Integer)map1.get(name)).intValue();
			writer1.append(String.format("%s       %s\r\n", key, value + ""));
			System.out.println(key + " " + value);  
		} 

		writer2.write("LOCATION STATISTICS:\r\n");
		writer2.append("\r\n");
		HashMap<String,Integer> map2 = (HashMap<String, Integer>) HashMapSorting.sortByValue(locationOccurrences);
		TableFormatter formatter2 = new TableFormatter("locations", new String[] {"occurrences", "location"});
		formatter2.fillTable1(map2);
		for (String loc: map2.keySet()){
			String key = loc.toString();
			int value = ((Integer)map2.get(loc)).intValue();
			writer2.append(String.format("%s       %s\r\n", key, value + ""));
			System.out.println(key + " " + value);  
		}

		writer3.write("OWNER STATISTICS:\r\n");
		writer3.append("\r\n");
		HashMap<String,Integer> map3 = (HashMap<String, Integer>) HashMapSorting.sortByValue(ownerOccurrences);
		TableFormatter formatter3 = new TableFormatter("users", new String[] {"posts", "username"});
		formatter3.fillTable1(map3);
		for (String owner: map3.keySet()){
			String key = owner.toString();
			int value = ((Integer)map3.get(owner)).intValue();
			writer3.append(String.format("%s       %s\r\n", key, value + ""));
			System.out.println(key + " " + value);  
		}

		HashMap<String,Integer> map4 = (HashMap<String, Integer>) HashMapSorting.sortByValue(tokenOccurrences);
		TableFormatter formatter4 = new TableFormatter("token", new String[] {"occurrences", "token"});
		formatter4.fillTable1(map4);
	}

	
	public boolean isNumber(String s) {
		if(s.equals("2020")) return false;
		
		try {  
		    Double.parseDouble(s);  
		    return true;
		  } catch(NumberFormatException e){  
		    return false;  
		  }  
	}


	public static JSONArray filterHashtags(JSONArray hashtags) {
		for(int h=0; h<hashtags.size(); h++) {
			if(fixedHashtags.contains((String)hashtags.get(h))) 
				hashtags.remove(h);						
		}

		return hashtags;
	}


	public static Stopwords setStopwords() throws FileNotFoundException, IOException {
		Stopwords sw = new Stopwords();
		sw.add("."); sw.add(":"); sw.add("("); sw.add(")"); sw.add("-"); sw.add("?"); sw.add(",");
		sw.add(".."); sw.add("..."); sw.add("*"); sw.add("??"); sw.add("???"); sw.add("/"); sw.add("+");
		sw.add("_"); sw.add("!"); sw.add("!!"); sw.add("!!!"); sw.add(":-"); sw.add("....."); sw.add("|");
		sw.add("~"); sw.add("="); sw.add("&"); sw.add("...."); sw.add("\"");

		File f = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\stopwords.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println("Stopword inserita: " + line);
				sw.add(line);
			}
		}

		return sw;
	}
	
	
	public void getHashtagsFrequences() throws IOException {
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_table.xlsx"));
		HashMap<Integer,Integer> frequences = new HashMap<Integer,Integer>();
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		int count = 0;
		int refValue = -1;
		
		// scorro il file per riga	
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			
			// salto la riga con gli header
			if(row.getRowNum()!=0 && row.getRowNum()<1048576) { 
				System.out.println("RIGA: " + row.getRowNum());				
				int corrValue = (int) row.getCell(0).getNumericCellValue(); // valore corrente di occorrenza che leggo
				if(row.getRowNum()==2)
					refValue = corrValue; // sto scorrendo gli hashtags con occorrenza pari a refValue
				
				/* se il valore è cambiato */
				if(corrValue != refValue) {
					frequences.put(refValue, count);
					refValue = corrValue;
					count = 1;
				} else count++;	
				
				if(row.getRowNum()==1048575) {
					frequences.put(corrValue, count);
				}
			}
		}
		
		HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) HashMapSorting.sortByValue1(frequences);
		TableFormatter formatter = new TableFormatter("hashtag_distribution",new String[]{"value","occurrences"});
		formatter.fillTable3(map);
	}

	
	public void getUsersFrequences() throws IOException {
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\users_table.xlsx"));
		HashMap<Integer,Integer> frequences = new HashMap<Integer,Integer>();
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		int count = 0;
		int refValue = -1;
		
		// scorro il file per riga	
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			
			// salto la riga con gli header
			if(row.getRowNum()!=0) { 
								
				int corrValue = (int) row.getCell(0).getNumericCellValue(); // valore corrente del numero di post dell'utente
				System.out.println("RIGA: " + row.getRowNum() + " VALORE: " + corrValue);
				if(row.getRowNum()==2)
					refValue = corrValue; // sto scorrendo gli hashtags con occorrenza pari a refValue
				
				/* se il valore è cambiato */
				if(corrValue != refValue) {
					frequences.put(refValue, count);
					refValue = corrValue;
					count = 1;
				} else count++;	
				
				if(row.getRowNum()==328108) {
					frequences.put(corrValue, count);
				}

			}
		}
		
		HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) HashMapSorting.sortByValue1(frequences);
		TableFormatter formatter = new TableFormatter("users_distribution",new String[]{"value","occurrences"});
		formatter.fillTable3(map);
	}
	

	public static String[] getHashtagsStatistics() {
		String[] stat = new String[3]; // 0 = min, 1 = media, 2 = max
		int min = 100, max = 0;
		float media = 0;
		int numOfPost = 0, numOfHashtags = 0;

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

							/* considero solo i file filtrati */
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

								/* scorro i singoli post */
								for(int m=0; m<posts.size(); m++) {
									numOfPost++;
									JSONArray hashtags = (JSONArray) ((JSONObject)posts.get(m)).get("Hashtags");
									if(hashtags!= null && hashtags.size()!=0) {
										hashtags = filterHashtags(hashtags);
										int size = hashtags.size();
										numOfHashtags = numOfHashtags + size;
										if(size<min)
											min = size;
										if(size>max)
											max = size;
									}
								}

								media = numOfHashtags/numOfPost;
								stat[0] = min + "";
								stat[1] = media + "";
								stat[2] = max + "";
							}

						}

					}
				}
			}
		}

		return stat;
	}


	public void elaborate() throws IOException, ParseException {
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
						//numOfFile = numOfFile + files.length;

						/* scorro i singoli file */
						for(int j=0; j<files.length; j++) {
							System.out.println(files[j]);
							parsable = true; 

							if(!files[j].getName().contains("filtered")) {
								filterInformations(files[j]);
								numOfFile++;
							}

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
		ArrayList<String> captionsNonAscii = new ArrayList<String>();
		HashMap<String,Integer> words = new HashMap<String,Integer>();
		int photos = 0, videos = 0, people = 0, text = 0, out = 0, in = 0, total = 0;
		int notascii = 0, cpt = 0;

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

								total = total + posts.size();
								for(int m=0; m<posts.size(); m++) {
									String caption = (String) ((JSONObject)posts.get(m)).get("AccessibilityCaption");

									if(caption!= null) {
										cpt++;

										if(caption.contains("Photo"))
											photos++;
										if(caption.contains("Video"))
											videos++; 
										if(caption.contains("people") || caption.contains("person"))
											people++;

										if(caption.contains("Image may contain:") && !caption.contains("text")) {
											String[] s = caption.split("contain: ");
											String cleanCaption;
											if(s[1].length()!=0) {
												cleanCaption = s[1].substring(0, s[1].length());
												if(cleanCaption.contains(",")) {
													String[] s1 = cleanCaption.split(", ");

													if(s1[s1.length-1].contains(".")) {													
														int index = s1[s1.length-1].indexOf(".");														
														s1[s1.length-1] = s1[s1.length-1].substring(0,index);
														//System.out.println(s1[s1.length-1]);
													}

													if(s1[s1.length-1].contains(" and ")) {
														String[] s2 = s1[s1.length-1].split(" and ");							

														if(words.containsKey(s2[0])) {
															words.put(s2[0], new Integer(words.get(s2[0])+1)) ;
														} 
														else words.put(s2[0], new Integer(1));	

														if(words.containsKey(s2[1])) {
															words.put(s2[1], new Integer(words.get(s2[1])+1)) ;
														} 
														else words.put(s2[1], new Integer(1));														
													}

													for(int a=0; a<s1.length-1; a++) {
														if(words.containsKey(s1[a])) {
															words.put(s1[a], new Integer(words.get(s1[a])+1)) ;
														} 
														else words.put(s1[a], new Integer(1));	
													}																																																																		
												} 

											} 
										}

										if(caption.contains("text")) {		
											if(words.containsKey("text")) {
												words.put("text", new Integer(words.get("text")+1)) ;
											} 
											else words.put("text", new Integer(1));

											if(caption.contains("text that says")) {
												int index = caption.indexOf("says");
												String s = caption.substring(index+5,caption.length());
												System.out.println("TESTO: " + s);

												if(!s.matches("\\A\\p{ASCII}*\\z")) {
													notascii++;
													System.out.println("NON ASCII: " + s);
													captionsNonAscii.add(s);
													break;
												} else captions.add(s);
											} 

											text++; 
										}

										if(caption.contains("indoor"))
											in++;										
										if(caption.contains("outdoor"))
											out++;

										//captions.add(caption);									
									} 
								}


							}

						}
					}
				}
			}
		}

		HashMap<String,Integer> map = (HashMap<String, Integer>) HashMapSorting.sortByValue(words);
		TableFormatter formatter = new TableFormatter("words", new String[]{"occurrences","word"});
		formatter.fillTable1(map);

		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(accessibilityStatistics), "utf-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		writer.write("---- ACCESSIBILITY STATISTICS: ----\r\n");
		writer.append("\r\n");
		writer.append("Number of media (total): " + total + "\r\n");
		writer.append("Number of media with accessibility caption: " + cpt + "\r\n");		
		writer.append("Number of media with people: " + people + "\r\n");
		writer.append("Number of media with text: " + text + "\r\n");
		writer.append("Number of media taken outdoor: " + out + "\r\n");
		writer.append("Number of media taken indoor: " + in + "\r\n");
		writer.append("Number of photos: " + photos + "\r\n");
		writer.append("Number of videos: " + videos + "\r\n");
		writer.append("Number of captions not ASCII: " + notascii + "\r\n");
		writer.append("\r\n");

		for(String c: captions) {
			try {
				writer.append(c + "\r\n");
				writer.flush();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

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
		} 
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

		//elab.elaborate();
		//elab.countOccurrences(); // statistiche su luoghi e hashtags
		//elab.getHashtagsFrequences();
		//elab.getUsersFrequences();
		//elab.lemmatizazion();

		elab.count();
		//elab.getAccessibilityCaptions();
	}	
}

