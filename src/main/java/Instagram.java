import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Instagram {
    public static WebDriver driver;
    public static ArrayList <String> posts; 
    public static int numOfPosts;

    
    public static void setDriver(WebDriver _driver){
        driver=_driver;
        driver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
    }

    
    /* login ad Instagram */
    public static void login(String user, String passw) {
    	System.out.println("Logging in...");
    	
    	/* reperisco la pagina del login e la carico */
        driver.get(Endpoint.LOGIN_URL);
            	
        /* inserisco username e password */
        driver.findElement(By.xpath(Xpaths.login_input_username)).sendKeys(user);
        driver.findElement(By.xpath(Xpaths.login_input_password)).sendKeys(passw);
        driver.findElement(By.xpath(Xpaths.login_btn)).click();
        
        driver.findElement(By.xpath("/html/body/div[1]/section/main/div/div/div/div/button")).click();
        
        /* disattivo notifiche (appare subito una finestra che lo chiede) */
        dismissLoginNotification();
        
        System.out.println("Logged.");      
    }

    
    /* clicca sul bottone "non ora" per evitare di attivare le notifiche di instagram */
    public static void dismissLoginNotification(){
        WebElement button = new WebDriverWait(driver, 10).until(driver -> driver.findElement(By.xpath(Xpaths.dismiss_notification_btn)));
        button.click();
    }
     
    
    /* cerca un hashtag su instagram */
    public static void searchHashtag(String hashtag) throws IOException {
    	/* se non contiene # lo inserisco all'inizio => in questo modo la ricerca viene facilitata (l'hashtag appare al primo posto) */
    	if(!hashtag.contains("#"))
    		hashtag = "#" + hashtag;
    	
    	driver.findElement(By.xpath(Xpaths.input_search_bar)).sendKeys(hashtag);
    	driver.findElement(By.xpath("/html/body/div[1]/section/nav/div[2]/div/div/div[2]/div[3]/div[2]/div/a[1]")).click(); // clicco invio
    	driver.findElement(By.xpath(Xpaths.warning_search_btn)).click();
    }
    
    
    /* leggo tutti gli hashtag salienti dal file hashtag.txt */
    public static String[] readHashtags() throws IOException {
    	String[] hashtags = new String[9];
    	File f = new File("./hashtag.txt");
    	FileReader reader = null;
    	
    	try {
    		reader = new FileReader(f);
    	} catch(FileNotFoundException e) {
    		e.printStackTrace();
    	}
    	
    	BufferedReader br = new BufferedReader(reader);
    	
    	int i = 0;
		String hashtag;
		while((hashtag=br.readLine())!=null) {
			hashtags[i]=hashtag;
			i++;
		}
		
		return hashtags;
    }
    
    /* scarica i post associati alla ricerca di "hashtag" e li salva in un file */
    public static void downloadData(String hashtag) throws IOException, ParseException, InterruptedException {
    	int colonna, riga;
		File f = new File("./" + hashtag + "_hashtag_data.txt");
		posts = new ArrayList<String>();
		numOfPosts = 0;
	
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		if(!f.exists())
			f.createNewFile();
		
		Thread timer = new Thread(new TimeOut());
		timer.start();    
		
		System.out.println("Post from hashtag: " + hashtag);
		
		/* finché non scade il timer per la ricerca e il download dei dati correlati all'hashtag */
		riga=1;
		while(timer.isAlive()) {
			while(true) {
				colonna=1;	
				Instagram.loadPosts(driver);

				System.out.println("---");
				while(colonna!=4) {
					try {		
						String elementPath = "/html/body/div[1]/section/main/article/div[2]/div/div[" + riga + "]/div[" + colonna + "]/a";
			    		//WebElement el = new WebDriverWait(driver, 10).until(driver ->  driver.findElement(By.xpath(elementPath)));
						WebDriverWait wait = new WebDriverWait(driver,40);
						WebElement el = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(elementPath)));
						
			    		String ref = el.getAttribute("href");
			    		String jsonUrl = ref + "?__a=1";
			    		
			    		String postFromUrl = Instagram.getPostJson(jsonUrl);
			    		
			    		Instagram.addData(f,postFromUrl);
					} catch(NoSuchElementException e) { }
					catch(Exception e1) { }
	
					//Instagram.loadPosts(driver);
		    		colonna++;
				} 
				
				if(riga==10)
					riga--;
				riga++;	
			}
		
		}
			
	}
	
    
    /* aggiunge il post al JSONArray che li raccoglie */
	private static void addData(File f, String postFromUrl) throws IOException {
    	/* creo un JSONObject in cui inserisco solamente le info che mi interessano */
    	//JSONObject post = new JSONObject();
    	
    	// N.B: Il parsing si far� poi, raccolgo tutto il json del post
    	
    	/*
    	post.put("AccessibilityCaption", ((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("accessibility_caption"));
    	JSONArray captionText = ((JSONArray)((JSONObject)((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("edge_media_to_caption")).get("edges"));
    	if(captionText!=null)
    		post.put("CaptionText",((JSONObject)((JSONObject)((JSONArray)((JSONObject)((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("edge_media_to_caption")).get("edges")).get(0)).get("node")).get("text"));
    	else post.put("CaptionText",null);
    		
    	post.put("NumberOfLikes",((JSONObject)((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("edge_media_preview_like")).get("count"));
    	post.put("NumberOfComments",((JSONObject)((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("edge_media_to_parent_comment")).get("count"));
    	post.put("Timestamp",((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("taken_at_timestamp"));
    	Timestamp t = new Timestamp((long) ((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("taken_at_timestamp"));
    	Date date=new Date(t.getTime());
    	post.put("LocalDate", "" + date);
    	
    	JSONObject location = (JSONObject) ((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("location");
    	if(location!=null && location.containsKey("name"))
        	post.put("Location",((JSONObject)((JSONObject)((JSONObject)((JSONObject)postFromUrl.get("graphql"))).get("shortcode_media")).get("location")).get("name"));
        else 
        	post.put("Location",null);
        */

    	/* se localmente ho salvato già 100 post li scrivo su file e svuoto l'array */
    	if(posts.size()==10) {
    		writeData(f);
    		posts = new ArrayList<String>();
    	}
    	
    	posts.add(postFromUrl);
    	numOfPosts++;
    	
    	System.out.println("Post " + numOfPosts + ":" + postFromUrl);
    }
    
    
    /* scrivo il contenuto corrente del JSONArray posts (append) */
	private static void writeData(File f) throws IOException {
		FileWriter fw = new FileWriter(f,true);	
		for(String p: posts)
			fw.write(p);
		fw.close();
	} 


	/* estraggo i dati utili in JSON */
    public static String getPostJson(String jsonUrl) throws IOException, MalformedURLException, ParseException {
    	URL url = new URL(jsonUrl);	
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(0);
		conn.setRequestMethod("GET");
		conn.setDoOutput(false);
		conn.connect();
			
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));		
		String jsonString = br.readLine();
		
		/*
		JSONParser parser = new JSONParser();		
		JSONObject post = (JSONObject) parser.parse(jsonString);
		*/
		return jsonString;
    }
    
    
    /* funzione di scrolling dei post di Instagram (ricerca hashtag) */
    public static void scrollPosts() {
    	try{
        	JavascriptExecutor js = (JavascriptExecutor) driver;
        	js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        	js.executeScript("window.scrollTo(0,-400)"); // a volte lo scrolling si blocca e per sbloccarlo basta tornare su e riscrollare
        	js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        }
        catch(Exception ignore){}
    }
    
    private static Boolean loadPosts(WebDriver driver) throws InterruptedException {
		long lastHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
		
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		
		try {
			long lH = lastHeight;
			
			new WebDriverWait(driver, 45).until(new ExpectedCondition<Boolean>() {
		        public Boolean apply(WebDriver driver) {                
		            return lH != (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
		        }
			});
		}catch(Exception e) { }
		
		long newHeight = (long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
        if (newHeight == lastHeight) {
        	return false;
        }
 
		return true;
	}

}
