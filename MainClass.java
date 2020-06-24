import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.*;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class MainClass {
    //public static boolean ended=false;
    public static void main(String [ ] args){
        HashMap<String, String> people=new HashMap<>();
        Scanner sc=new Scanner(System.in);
        String regex="\\\\{1,2}(?!\\\\)";
      
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(6000);
        
        /* crea un selenium proxy prendendo come argomento un browser mob proxy  */
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        seleniumProxy.setHttpProxy("localhost:" + proxy.getPort());
        seleniumProxy.setSslProxy("localhost:" + proxy.getPort());
        
        
        String[] hashtags = null;
        try {
			hashtags = Instagram.readHashtags();
		} catch (IOException e1) {
			System.out.println("Errore nel reperimento degli hashtags.");
			e1.printStackTrace();
		}
        
        ProfilesIni profile = new ProfilesIni();
        FirefoxProfile firefoxProfile = profile.getProfile("Martina");
        firefoxProfile.setAcceptUntrustedCertificates(true);
        firefoxProfile.setAssumeUntrustedCertificateIssuer(false);
        firefoxProfile.setPreference("security.insecure_field_warning.contextual.enabled", false);    
        
        //DesiredCapabilities capabilities = new DesiredCapabilities();
        //capabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
        //capabilities.setCapability("marionette", true);
        //capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        //capabilities.setCapability("acceptInsecureCerts", true);
        //capabilities.setAcceptInsecureCerts(true);
        
        
        /* setto il driver */
        System.setProperty("webdriver.gecko.driver", "./geckodriver");
        
        /* setto le sue opzioni */
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        //firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy); //=> questa istruzione dà errore "Connessione non sicura"
        //firefoxOptions.setAcceptInsecureCerts(true);
        
        firefoxOptions.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
        firefoxOptions.setCapability("marionette", true);
        firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy);
        firefoxOptions.setCapability("acceptInsecureCerts", true);
        firefoxOptions.setCapability("acceptSslCerts", true);
        
        
        /* creo un FirefoxDriver */
		WebDriver driver = new FirefoxDriver(firefoxOptions);
 
        
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        /* setto il tipo HAR (HttpArchive) come il tipo del file che verrà utilizzato per tenere i dati delle richieste HTTP */
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        Instagram.setDriver(driver);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        
        try {     	
        	Instagram.login("","");
        	
        	System.out.println("Retrieving data from hot hashtags...");	
        	
        	for(int i=0; i<hashtags.length; i++) {
        		Instagram.searchHashtag(hashtags[i]);
        		int searchTime = 18000000; // 30 minuti
        		
        		Instagram.downloadData(hashtags[i]);
        		
        		Thread.sleep(searchTime);
        		      		
        	}
        	

        } catch(Exception e) {
        	e.printStackTrace();
        }
    }
    
}

    /*
    
    @SuppressWarnings("deprecation")
	private static JsonNode getUserInformation(String name, BrowserMobProxy proxy, ConcurrentHashMap<String, List<JsonNode>> listenedResponse) throws InterruptedException, IOException {
        System.out.println("Getting advanced of: " + name);
        JsonNode finalJson = instagram.getProfileAdvancedJson(name);

        proxy.newHar("profile " + name);
        instagram.openProfile(name);
        Thread.sleep(3000);

        Har har = proxy.getHar();
        JsonNode rootNode = proxyUtils.getJsonFromHar(har);

        JsonNode entriesNode = rootNode.path("log").get("entries");

        /* se l'account non è privato 
        if(!finalJson.get("is_private").asBoolean()) {
            //Aggiungo storie in evidenza
            System.out.println("Getting highlight of: " + name);
            for (JsonNode node : entriesNode) {
                ObjectMapper mapper = new ObjectMapper();
                if (Pattern.compile(".*" + endpoint.highlightMedia + ".*").matcher(node.path("request").get("url").asText()).matches()) {
                    JsonNode userNode = node.path("response").path("content")
                            .get("text");
                    JsonNode info = mapper.readTree(userNode.asText());
                    ((ObjectNode) finalJson).put("edge_highlight_reel", info.path("data").get("user").get("edge_highlight_reels"));
                    break;
                }
            }


            if (finalJson.path("edge_followed_by").get("count").asInt() < 2000) {
                System.out.println("Getting followers of: " + name);
                try {
                    JsonNode info = instagram.getAllFollowers(listenedResponse);
                    ((ObjectNode) (finalJson.get("edge_followed_by"))).put("edges", info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            if (finalJson.path("edge_follow").get("count").asInt() < 2000) {
                instagram.openProfile(name);
                System.out.println("Getting following of: " + name);
                try {
                    JsonNode info = instagram.getAllFollowing(listenedResponse);
                    ((ObjectNode) (finalJson.get("edge_follow"))).put("edges", info);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            //prtendo hashtag
            instagram.openProfile(name);
            System.out.println("Getting followed hashtag of: " + name);
            try {
                JsonNode info = instagram.getAllFollowedHashtag(listenedResponse);
                ((ObjectNode) finalJson).put("edge_following_hashtag", info);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Getting advanced media of: " + name);

            for (JsonNode node : finalJson.path("edge_owner_to_timeline_media").get("edges")) {

                String shortCode = node.path("node").get("shortcode").asText();
                Thread.sleep((int) (Math.random() * 3000));
                ((ObjectNode) node).put("advanced", instagram.getMediaAdvancedJson(shortCode));
                //((ArrayNode)finalJson.path("edge_owner_to_timeline_media").path("edges")).add(media);

                //System.out.println(node.toPrettyString());
            }

            System.out.println("Getting tagged media");
            instagram.openProfile(name);
            
            //TODO entrare e prendere l'advanced dei media taggati
            ((ObjectNode) finalJson).put("edge_user_to_photos_of_you", instagram.getTaggedPost(listenedResponse));
            System.out.println("Got tagged media");
            //System.out.println(finalJson.toPrettyString());
        }
        
        return finalJson;
    }
    */
    


