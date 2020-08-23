import net.lightbody.bmp.*;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.remote.CapabilityType;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
 

public class MainClass {
    public static void main(String [ ] args) {
    	System.out.println("--- Instagram Crawler ---");
    	
    	BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);
        
        /* crea un selenium proxy prendendo come argomento un browser mob proxy  */
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        seleniumProxy.setHttpProxy("localhost:" + proxy.getPort());
        seleniumProxy.setSslProxy("localhost:" + proxy.getPort());

        /* setto il driver */
        System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");
        
        /* setto le sue opzioni */
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy);        
        firefoxOptions.setCapability("marionette", true);
        firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy);
        firefoxOptions.setCapability("acceptInsecureCerts", true);
        firefoxOptions.setCapability("acceptSslCerts", true);
      
        /* creo un FirefoxDriver */
        WebDriver driver = new FirefoxDriver(firefoxOptions);
      
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        /* setto il tipo HAR (HttpArchive) come il tipo del file che verr√† utilizzato per tenere i dati delle richieste HTTP */
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        Instagram.setDriver(driver);
        while(true) {
	        try {     	
	        	Instagram.login("mrtns_95","martina95");
	        	
	        	System.out.println("Retrieving data from hot hashtags...");	
	        	
	        	/* HASHTAGS SEARCH */
	        	
	        	// covid_19, covid19, covid
	        	Instagram.searchAndDownload("#cov",3);   
	     
	        	Instagram.searchAndDownload("#coronavirus",1);       	
	        	
	        	// quarantine, quarantena, quarantinelife
	        	Instagram.searchAndDownload("#quara",3);
	        	
	        	Instagram.searchAndDownload("#lockdown",1);  
	        	Instagram.searchAndDownload("#lockdowndiaries",1);  
	        	Instagram.searchAndDownload("#stayhome",1); 
	        	
	        	// socialdistancing, socialdistance
	        	Instagram.searchAndDownload("#socialdistanc",2);  
	        	
	        	// pandemic, pandemic2020
	        	Instagram.searchAndDownload("#pandemic",2);  
	        	
	        	Instagram.searchAndDownload("#andr‡tuttobene", 1);
	 
	        } catch(Exception e) {
	        	e.printStackTrace(); 
	        } 
        }
        
        /* chiusura 
	    proxy.stop();
	    driver.close(); */
    }
    
}

    


