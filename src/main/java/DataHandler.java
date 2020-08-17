import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.json.simple.*;
import org.json.simple.parser.*;


public class DataHandler implements Runnable {
	static File data;
	JSONObject fileData;
	static JSONArray oldFileList;
	JSONArray newFileList;
	static boolean fail;
	static String currentHashtag;

	
	public DataHandler(File _data) throws IOException {
		data = _data;
		fail = false;
		
		if(!data.exists()) {
			System.out.println("Creo il file...");
			data.createNewFile(); 
			System.out.println("File creato: " + data.getName());
		} 
	}
	
	
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) { }
		
		while(true) {
				try {
					Thread.sleep(8000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				if(data.length()!=0) {
					JSONParser parser = new JSONParser();	
					oldFileList = new JSONArray();
					
					try(FileReader fr = new FileReader(data)) {
						Object obj = (JSONArray) parser.parse(fr);
						oldFileList = (JSONArray) obj;
					} catch (Exception e1) {
						e1.printStackTrace();
						fail = true;
					}
										
				} else { /* il file è vuoto, creo un JSONArray */
					oldFileList = new JSONArray();
				}
				
				/* aggiungo ai dati vecchi quelli che nel frattempo ho scaricato */
				newFileList = Instagram.posts;
				for(int i=0; i < newFileList.size(); i++) {
					oldFileList.add(oldFileList.size(), newFileList.get(i));
				}
				
				try {
					writeData(oldFileList);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				newFileList.clear();	
				
				/* uso e aggiorno il file di backup contenente i dati scaricati (fault tolerance) */
				Path backupFile = Paths.get("C:\\Users\\marti\\git\\TirocinioProtano\\backup_" + currentHashtag + "_hashtag_data.txt");
			    Path originalFile = data.toPath();
			    try {
			    	if(!fail) {
			    		Files.copy(originalFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
			    	} else { /* restoring da backup */
			    		System.err.println("ERRORE: restoring da backup!");
			    		Files.copy(backupFile, originalFile, StandardCopyOption.REPLACE_EXISTING);
			    	}
			    	
			    	fail = false;
				} catch (IOException e) {
					e.printStackTrace();
				}			
		}
	}
	
	/* scrivo i dati sul file */
	public static void writeData(JSONArray array) throws IOException {
		try(FileWriter fw = new FileWriter(data, false)){
			fw.write(array.toJSONString());
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	} 
	
	public static void setHashtag(String hashtag) {
		currentHashtag = hashtag;
	}

}
