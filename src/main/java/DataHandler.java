import java.io.*;
import java.io.IOException;
import org.json.simple.*;
import org.json.simple.parser.*;


public class DataHandler implements Runnable {
	static File data;
	JSONObject fileData;
	static JSONArray oldFileList;
	JSONArray newFileList;

	
	public DataHandler(File _data) throws IOException {
		data = _data;
		
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
					oldFileList.clear();
					try {
						oldFileList = (JSONArray) parser.parse(new FileReader(data));
					} catch (IOException | ParseException e) {
						e.printStackTrace();
					}
				} else {
					oldFileList = new JSONArray();
				}
				
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
		} 
		
	}
	
	/* scrivo i dati sul file */
	public static void writeData(JSONArray array) throws IOException {
		FileWriter fw = new FileWriter(data);	

		fw.write(array.toJSONString());
		
		fw.close();
	} 

}
