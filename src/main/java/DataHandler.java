import java.io.*;
import java.sql.Timestamp;
import org.json.simple.*;


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
	public void writeData(JSONArray array, boolean finished) throws IOException {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(data), "utf-8"));
		    writer.write(array.toJSONString());
		    writer.flush();
		} catch (IOException ex) {
		    // Report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		
		/* creo il file per mantenere i prossimi dati */
		if(!finished)
			createDataFile();
	} 
}
