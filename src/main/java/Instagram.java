import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Instagram {
    public static WebDriver driver;

    public static void setDriver(WebDriver _driver){
        driver=_driver;
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
    	driver.findElement(By.xpath("/html/body/div[1]/section/nav/div[2]/div/div/div[2]/div[2]/div[2]/div/a[1]/div/div/div[2]/span/span")).click(); // clicco invio
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
			System.out.println(hashtag);
			hashtags[i]=hashtag;
			i++;
		}
		
		return hashtags;
    }
    
    /* scarica i post associati alla ricerca di "hashtag" e li salva in un file */
    public static void downloadData(String hashtag) throws IOException, ParseException {
    	int colonna, riga;
		File f = new File("./" + hashtag + "_hashtag_data.txt");
	
		if(!f.exists())
			f.createNewFile();
		
		Thread timer = new Thread(new TimeOut());
		timer.start();    
		
		System.out.println("Post from hashtag: " + hashtag);
		/* finché non scade il timer per la ricerca e il download dei dati correlati all'hashtag */
		while(timer.isAlive()) {
			riga=1;
			while(riga!=4) {
				colonna=1;	
				while(colonna!=4) {
					Instagram.scrollFollow();
					String temp = "html/body/div[1]/section/main/article/div[1]/div/div/div[" + riga + "]/div[" + colonna + "]/a/div/div[2]";
		    		String ref = driver.findElement(By.xpath("/html/body/div[1]/section/main/article/div[1]/div/div/div[" + riga + "]/div[" + colonna + "]/a")).getAttribute("href");
		    		// "/html/body/div[1]/section/main/article/div[1]/div/div/div[" + riga + "]/div[" + colonna + "]/a"
		    		String jsonUrl = ref + "?__a=1";
		    		//driver.get(jsonUrl);
		    	  	
		    		
		    		JSONObject post = Instagram.getPostJson(jsonUrl);
		    		Instagram.writeData(f,post);
	
		    		colonna++;
				}
				
				riga++;	
			}
		
		}
		
		
		
		
		
	}
    
    /* scrive l'oggetto JSON corrispondente a un post sul file */
    private static void writeData(File f, JSONObject post) throws IOException {
		FileWriter fw = new FileWriter(f,true);
		
		fw.write(post.toJSONString());
		System.out.println(post.toJSONString());
		fw.close();
	}


	/* estraggo i dati utili in JSON */
    public static JSONObject getPostJson(String jsonUrl) throws IOException, MalformedURLException, ParseException {
    	URL url = new URL(jsonUrl);	
		URLConnection conn = url.openConnection();
		conn.connect();
			
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));		
		String jsonString = br.readLine();
		
		JSONParser parser = new JSONParser();		
		JSONObject post = (JSONObject) parser.parse(jsonString);
	
		
		return post;
    }
  
    
    /*  funzione che apre il profilo di un utente */
    public static void openProfile(String username){
        driver.get(Endpoint.get_account_page_link(username));
    }
    
    
    /* restituisce il profilo dell'utente "user" in formato JSON */
    public static JsonNode getProfileAdvancedJson(String user){
        //remove
        //edge_mutual_followed_by
        //profile_pic_url

        driver.get(Endpoint.get_account_json_link(user));
        String jsonString=driver.findElement(By.tagName("pre")).getText();
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj=null;
        try {
            actualObj = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        /* restituisce l'oggetto JSON associato a "user" nell'oggetto JSON che rappresenta l'intero profilo */
        return actualObj.path("graphql").get("user");
    }

    
    public static JsonNode getMediaAdvancedJson(String mediaShortID){
        //todo prendere qualche like (100)?
        driver.get(Endpoint.get_media_json_link(mediaShortID));
        
        String jsonString=driver.findElement(By.tagName("pre")).getText();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj=null;
        try {
            actualObj = mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return actualObj.path("graphql").get("shortcode_media");
    }

    
    public static void openFollowers(){
        WebElement button = new WebDriverWait(driver, 10).until(driver -> driver.findElement(By.xpath(Xpaths.follower)));
        button.click();
    }

    
    public static JsonNode getAllFollowers(ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        openFollowers();
        
        ArrayNode followers= JsonNodeFactory.instance.arrayNode();
        while(!ProxyUtils.endedFollower){
            try {
                if(ProxyUtils.Limited){
                    driver.navigate().refresh();
                    listenedResponse.put("Follower", new ArrayList<>());
                    Thread.sleep(3000);
                    ProxyUtils.Limited=false;
                    openFollowers();
                }
               Instagram.scrollFollow();
                Thread.sleep((int)(Math.random()*3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        ProxyUtils.endedFollower=false;
        for(JsonNode node:listenedResponse.get("Follower")){
            followers.addAll((ArrayNode) node.path("data").path("user").path("edge_followed_by").get("edges"));
        }
        listenedResponse.put("Follower", new ArrayList<>());

        return followers;
    }

    
    public static void scrollFollow() {
        try{
        	JavascriptExecutor js = (JavascriptExecutor) driver;
        	js.executeScript("document.getElementsByClassName('"+Xpaths.followScrollDiv+"')[0].scrollTo(0,document.getElementsByClassName('"+Xpaths.followScrollDiv+"')[0].scrollHeight)");
        }
        catch(Exception ignore){}
    }

    
    public static void openFollowing(){
        WebElement button = new WebDriverWait(driver, 10).until(driver -> driver.findElement(By.xpath(Xpaths.following)));
        button.click();
    }

    public static JsonNode getAllFollowing(ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        openFollowing();
        
        ArrayNode followers= JsonNodeFactory.instance.arrayNode();
        while(!ProxyUtils.endedFollowing){
            try {
                if(ProxyUtils.Limited){
                    driver.navigate().refresh();
                    listenedResponse.put("Following", new ArrayList<>());
                    Thread.sleep(3000);
                    ProxyUtils.Limited=false;
                    openFollowing();
                }
                
                Instagram.scrollFollow();
                Thread.sleep((int)(Math.random()*3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ProxyUtils.endedFollowing=false;
        for(JsonNode node:listenedResponse.get("Following")){
            followers.addAll((ArrayNode) node.path("data").path("user").path("edge_follow").get("edges"));
        }
        listenedResponse.put("Following", new ArrayList<>());
        return followers;
    }

    
    public static void scrollFollowing() {
        try{
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.getElementsByClassName('"+Xpaths.followScrollDiv+"')[0].scrollTo(0,document.getElementsByClassName('"+Xpaths.followScrollDiv+"')[0].scrollHeight)");
        }
        catch(Exception ignore){}
    }

    public static JsonNode getAllFollowedHashtag(ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        openHashtag();
        while(!ProxyUtils.endedHashtag){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ProxyUtils.endedHashtag=false;
        return listenedResponse.get("Hashtag").remove(0);
    }

    private static void openHashtag() {
        openFollowing();
        WebElement button = new WebDriverWait(driver, 10).until(driver -> driver.findElement(By.xpath(Xpaths.hashtagFollowedButton)));
        button.click();
    }

    @SuppressWarnings("deprecation")
	public static JsonNode getTaggedPost(ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        ProxyUtils.interceptTagged=true;
        openTaggedPost();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayNode tagged=JsonNodeFactory.instance.arrayNode();
        ProxyUtils.interceptTagged=false;
        for (JsonNode n:listenedResponse.get("Tagged")){
            tagged.addAll((ArrayNode)n);
        }
        //prendo advanced per ognuno
        for(JsonNode media: tagged){
            ((ObjectNode)media).put("advanced", getMediaAdvancedJson(media.path("node").get("shortcode").asText()));
        }
        listenedResponse.put("Tagged", new ArrayList<>());
        return tagged;
    }

    private static void openTaggedPost(){
        WebElement button = new WebDriverWait(driver, 10).until(driver -> driver.findElement(By.xpath(Xpaths.taggedPost)));
        button.click();
    }



}
