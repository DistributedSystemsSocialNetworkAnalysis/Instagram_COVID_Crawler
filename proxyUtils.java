import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class proxyUtils {
    public static boolean endedFollower=false;
    public static boolean endedFollowing=false;
    public static boolean endedHashtag=false;
    public static boolean Limited=false;
    public static boolean interceptTagged=false;


    public static void blockURL(String url, int responseCode, BrowserMobProxy proxy){
        proxy.addRequestFilter((request, contents, messageInfo) -> {
            if (Pattern.compile(url).matcher(messageInfo.getOriginalUrl()).matches()) {
                System.out.println(contents);
                final HttpResponse response = new DefaultHttpResponse(
                        request.getProtocolVersion(),
                        HttpResponseStatus.valueOf(responseCode));

                response.headers().add(HttpHeaders.CONNECTION, "Close");

                return response;
            }
            return null;
        });
    }

    public static void interceptFollower(String url, BrowserMobProxy proxy, ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        ObjectMapper mapper = new ObjectMapper();
        proxy.addResponseFilter((httpResponse, httpMessageContents, httpMessageInfo) -> {
            if(Pattern.compile(url).matcher(httpMessageInfo.getOriginalUrl()).matches()){
                try {
                    Thread.sleep((int)(Math.random()*3000));
                    JsonNode n=mapper.readTree(httpMessageContents.getTextContents());

                    if (n.path("data").path("user").path("edge_followed_by").path("page_info")
                            .get("has_next_page").asBoolean()) {
                        listenedResponse.get("Follower").add(n);
                        //instagram.scrollFollow();
                        //instagram.scrollFollow();
                    }
                    else{
                        listenedResponse.get("Follower").add(n);
                        System.out.println("Got all followers");
                        endedFollower=true;
                    }
                } catch (Exception e) {
                    Limited=true;
                    e.printStackTrace();
                }
            }
        });
    }

    public static void interceptFollowing(String url, BrowserMobProxy proxy, ConcurrentHashMap<String, List<JsonNode>> listenedResponse){
        ObjectMapper mapper = new ObjectMapper();
        proxy.addResponseFilter((httpResponse, httpMessageContents, httpMessageInfo) -> {
            if(Pattern.compile(url).matcher(httpMessageInfo.getOriginalUrl()).matches()){
                try {
                    Thread.sleep((int)(Math.random()*3000));
                    JsonNode n=mapper.readTree(httpMessageContents.getTextContents());
                    if (n.path("data").path("user").path("edge_follow").path("page_info")
                            .get("has_next_page").asBoolean()) {

                        listenedResponse.get("Following").add(n);
                        //instagram.scrollFollow();
                        //instagram.scrollFollow();
                    }
                    else{
                        listenedResponse.get("Following").add(n);
                        System.out.println("Got all following");
                        endedFollowing=true;
                    }

                } catch (Exception e) {
                    Limited=true;
                    e.printStackTrace();
                }
            }
        });
    }

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


    public static JsonNode getJsonFromHar(Har har){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            har.writeTo(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode=null;
        try {
            rootNode = mapper.readTree(bos.toString("UTF-8"));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return rootNode;
    }


    public static void interceptTaggedPost(String url, BrowserMobProxy proxy, ConcurrentHashMap<String, List<JsonNode>> listenedResponse) {
        ObjectMapper mapper = new ObjectMapper();
        proxy.addResponseFilter((httpResponse, httpMessageContents, httpMessageInfo) -> {
            if (Pattern.compile(url).matcher(httpMessageInfo.getOriginalUrl()).matches() && interceptTagged) {
                try {
                    System.out.println(httpMessageContents.getTextContents());
                    JsonNode n = mapper.readTree(httpMessageContents.getTextContents());
                    listenedResponse.get("Tagged").add(n.path("data").path("user")
                            .path("edge_user_to_photos_of_you").get("edges"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

