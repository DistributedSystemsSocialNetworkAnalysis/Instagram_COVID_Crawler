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
        proxy.start(0);
        
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

    


