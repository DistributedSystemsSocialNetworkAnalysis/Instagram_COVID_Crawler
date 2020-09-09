import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lightbody.bmp.BrowserMobProxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ProxyUtils {
    public static boolean endedFollower=false;
    public static boolean endedFollowing=false;
    public static boolean endedHashtag=false;
    public static boolean Limited=false;
    public static boolean interceptTagged=false;

    public static void interceptHashTag(String url, BrowserMobProxy proxy, ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        ObjectMapper mapper = new ObjectMapper();
        proxy.addResponseFilter((httpResponse, httpMessageContents, httpMessageInfo) -> {
            if(Pattern.compile(url).matcher(httpMessageInfo.getOriginalUrl()).matches()){
                try {
                    Thread.sleep((int)(Math.random()*3000));
                    JsonNode n=mapper.readTree(httpMessageContents.getTextContents());
                    listenedResponse.get("Hashtag").add(n.path("data").path("user").get("edge_following_hashtag"));
                    System.out.println("Got all hashtag");
                    endedHashtag=true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}

