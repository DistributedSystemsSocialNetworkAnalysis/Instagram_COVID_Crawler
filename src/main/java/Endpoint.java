import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class Endpoint {
    public static String USER_MEDIAS = "17880160963012870";
    public static String USER_STORIES = "17890626976041463";
    public static String STORIES = "17873473675158481";

    public static String followerQueryHash="c76146de99bb02f6415203be841dd25a";
    public static String followingQueryHash="d04b0a864b4b54837c0d870b0e77e076";
    public static String hashtagQueryHash="e6306cc3dbe69d6a82ef8b5f8654c50b";
    public static String taggedpostQueryHash="ff260833edf142911047af6024eb634a";
    public static String PROFILE="e74d51c10ecc0fe6250a295b9bb9db74";
    public static String PROFILE_MEDIA="e769aa130647d2354c40ea6a439bfc08";
    public static String highlightMedia="ad99dd9d3646cc3c0dda65debcd266a7";
    public static String BASE_URL = "https://www.instagram.com";
    public static String LOGIN_URL = "https://www.instagram.com/accounts/login/";
    public static String PROFILE_QUERY="https://www.instagram.com/graphql/query/?query_hash=e74d51c10ecc0fe6250a295b9bb9db74&variables=%s";
    public static String PROFILE_FEED_QUERY="https://www.instagram.com/graphql/query/?query_hash=e769aa130647d2354c40ea6a439bfc08&variables=%s";
    public static String ACCOUNT_PAGE = "https://www.instagram.com/%s";
    public static String MEDIA_LINK = "https://www.instagram.com/p/%s";
    public static String ACCOUNT_MEDIAS = "https://www.instagram.com/graphql/query/?query_hash=42323d64886122307be10013ad2dcc44&variables=%s";
    public static String ACCOUNT_JSON_INFO = "https://www.instagram.com/%s/?__a=1";
    public static String MEDIA_JSON_INFO = "https://www.instagram.com/p/%s/?__a=1";
    public static String GENERAL_SEARCH = "https://www.instagram.com/web/search/topsearch/?query=%s";
    public static String COMMENTS_BEFORE_COMMENT_ID_BY_CODE = "https://www.instagram.com/graphql/query/?query_hash=97b41c52301f77ce508f55e66d17620e&variables=%s";
    public static String LIKES_BY_SHORTCODE_OLD = "https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables={\"shortcode\":\"%s\",\"first\":%s,\"after\":\"%s\"}";
    public static String LIKES_BY_SHORTCODE = "https://www.instagram.com/graphql/query/?query_hash=d5d763b1e2acf209d62d22d184488e57&variables=%s";
    public static String FOLLOWING_URL_OLD = "https://www.instagram.com/graphql/query/?query_id=17874545323001329&id={{accountId}}&first={{count}}&after={{after}}";
    public static String FOLLOWING_URL = "https://www.instagram.com/graphql/query/?query_hash=d04b0a864b4b54837c0d870b0e77e076&variables=%s";
    public static String FOLLOWERS_URL_OLD = "https://www.instagram.com/graphql/query/?query_id=17851374694183129&id={{accountId}}&first={{count}}&after={{after}}";
    public static String FOLLOWERS_URL = "https://www.instagram.com/graphql/query/?query_hash=c76146de99bb02f6415203be841dd25a&variables=%s";
    public static String INSTAGRAM_CDN_URL = "https://scontent.cdninstagram.com/";
    public static String ACCOUNT_JSON_PRIVATE_INFO_BY_ID = "https://i.instagram.com/api/v1/users/%s/info/";

    public static String ACCOUNT_MEDIAS2 = "https://www.instagram.com/graphql/query/?query_id=17880160963012870&id={{accountId}}&first=10&after=";

    public static String GRAPH_QL_QUERY_URL = "https://www.instagram.com/graphql/query/?query_id=%s";

    int request_media_count = 30;


    /* metodo che restituisce il link dell'utente "username" */
    public static String get_account_page_link(String username){
            return String.format(ACCOUNT_PAGE, username);
    }

    
    /* metodo che restituisce il link del profilo di un utente "username" in formato JSON */
    public static String get_account_json_link(String username){
        return String.format(ACCOUNT_JSON_INFO, username);
    }

    
    /* restituisce delle informazioni private di un account fornendo il corrispondente id */
    public static String get_account_json_private_info_link_by_account_id(String account_id){
        return String.format(ACCOUNT_JSON_PRIVATE_INFO_BY_ID, account_id);
    }


    public static String get_account_medias_json_link(Map<String, String> variables){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/
    	String json=null;
        return String.format(ACCOUNT_MEDIAS, json);
    }


    public static String get_media_page_link(String code){
        return String.format(MEDIA_LINK, code);
    }


    public static String get_media_json_link(String code){
        return String.format(MEDIA_JSON_INFO, code);
    }


    public static String get_general_search_json_link(String query){
        return String.format(GENERAL_SEARCH, query);
    }


    public static String get_comments_before_comments_id_by_code(Map<String, String> variables){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/String json=null;
        return String.format(COMMENTS_BEFORE_COMMENT_ID_BY_CODE, json);
    }


    public static String get_last_likes_by_code_old(String code, String count, String last_like_id){
        return String.format(LIKES_BY_SHORTCODE_OLD, code, count, last_like_id);
    }


    public static String get_last_likes_by_code(Map<String, String> variables){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/String json=null;
        return String.format(LIKES_BY_SHORTCODE, json);
    }


    public static String get_followers_json_link_old(String account_id, String count, String after/*=""*/){
        String url = new String(FOLLOWERS_URL_OLD);
        url=url.replace("{{accountId}}", account_id);
        url = url.replace("{{count}}", count);

        if (after == null){
            url = url.replace("&after={{after}}", "");
        }
        else{
            url = url.replace("{{after}}", after);
        }
        return url;
    }


    public static String get_followers_json_link(Map<String, String> variables){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/String json=null;
        return String.format(FOLLOWERS_URL, json);
    }


    public static String get_following_json_link_old(String account_id, String count, String after/*=""*/){
        String url = new String(FOLLOWING_URL_OLD);
        url=url.replace("{{accountId}}", account_id);
        url = url.replace("{{count}}", count);

        if (after == null){
            url = url.replace("&after={{after}}", "");
        }
        else{
            url = url.replace("{{after}}", after);
        }
        return url;

    }

    public static String get_following_json_link(Map<String, String> variables){
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/String json=null;
        return String.format(FOLLOWING_URL, json);
    }

    public static String get_user_stories_link(){
        //return get_graph_ql_url(USER_STORIES, (new JSONObject().put("variables","")).toString());
        return null;
    }


    public static String get_graph_ql_url(String query_id, String parameters){
        String url = String.format(GRAPH_QL_QUERY_URL, query_id);

        if (parameters.length() > 0) {
            String query_string = null;
            try {
                query_string = URLEncoder.encode(parameters, "UTF-8");
                url += "&" + query_string;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return url;
    }


    public static String get_stories_link(Map<String, String> variables) {
        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json=gson.toJson(variables);*/String json=null;
        return get_graph_ql_url(STORIES, json);
    }
}
