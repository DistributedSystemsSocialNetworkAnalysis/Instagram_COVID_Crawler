import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;

import org.json.simple.*;
import org.json.simple.parser.*;


public class DataHandler {
	static File data;
	JSONObject fileData;
	JSONArray oldFileList;
	JSONArray newFileList;
	String currentHashtag;
	static int numOfFile;

	
	public DataHandler(String hashtag) throws IOException {
		numOfFile = 1; 
		currentHashtag = hashtag;
	}
	
	@SuppressWarnings("deprecation")
	public void createDataFile() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		int day = timestamp.getDate();
		int month = timestamp.getMonth()+1;
		String date = "(" + day + "-" + month + "-" + "20" + ")";
		String fileName = currentHashtag + "[" + numOfFile + " - " + date + "]" + "_hashtag_data.json";
    	data = new File("C:\\Users\\marti\\git\\TirocinioProtano\\" + fileName);
    	
    	if(!data.exists()) {
			System.out.println("Creo il file...");
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			System.out.println("File creato: " + data.getName());
		}
    	
    	numOfFile++;
	}
	
	
	/* scrivo i dati sul file */
	public void writeData(JSONArray array) throws IOException {
		/* scrivo i dati che ho adesso (500 post) sul file corrente */
		try(FileWriter fw = new FileWriter(data, false)){
			fw.write(Instagram.posts.toJSONString());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		/* creo il file per mantenere i prossimi dati */
		createDataFile();
	} 
}
