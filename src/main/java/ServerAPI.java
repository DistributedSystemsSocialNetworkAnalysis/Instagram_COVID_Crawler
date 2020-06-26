import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerAPI {
    public static boolean sendIgJson(String id, JsonNode json, int type){

        String json_path=json.toString();
        URL url = null; 
        try {
            url = new URL("http://192.168.178.56/send_ig.php");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("id_ig", id);
            arguments.put("json_ig", json_path);
            arguments.put("type", type+"");
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));
            //System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    

    public static ArrayList<String> getNeededIg(int type){
        ArrayList<String> nickname=new ArrayList<>();
        
        JsonNode n=null;
        try {
            URL url = new URL("http://192.168.178.56/get_needed_ig.php");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("type", type+"");
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            
            http.connect();

            int responseCode = http.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            /* la richiesta è terminata con successo */
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                ObjectMapper mapper = new ObjectMapper();
                n=mapper.readTree(response.toString());
            } else { 
            	/* la richiesta non ha dato esito positivo */
                System.out.println("POST request not worked");
            }

            http.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        switch (type) {
        	case 1: {
        		
        		
        		
        	}
            case 2: {
                if (n != null) {
                	/* aggiunge la lista dei profili IG all'ArrayList "nickname" che verrà restituito */
                    for (JsonNode jsonNode : (ArrayNode) n.get("ig_profiles")) {
                        String url = jsonNode.get("json_path").asText();
                        String[] tokens = url.split("/");
                        String name = tokens[tokens.length - 1];
                        nickname.add(name);
                    }
                }
                break;
            }
            case 4: {
                if (n != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    
                    for (JsonNode jsonNode : (ArrayNode) n.get("ig_profiles")) {
                        JsonNode json=null;
                        //String id=json.get("id").asText();
                        // era sbagliato? è giusto quello sotto
                        
                        String id=jsonNode.get("id").asText();
                        try {
                            json = mapper.readTree(jsonNode.get("json_path").asText());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        
                        if(json.get("is_private").asBoolean()){
                            //era privato va in eccezione non trova edges
                            updateType(id,0);
                        }
                        else {
                            try {
                            	/* raccoglie follower */
                                for (JsonNode node : (ArrayNode) json.path("edge_followed_by").get("edges")) {
                                    nickname.add(id+" "+node.path("node").get("username").asText());
                                }
                                                  
                                /* raccoglie following */
                                for (JsonNode node : (ArrayNode) json.path("edge_follow").get("edges")) {
                                    nickname.add(id+" "+node.path("node").get("username").asText());
                                }
                            } catch(Exception e) {
                            	e.printStackTrace();
                            }
                            
                            nickname.add(json.get("id").asText());
                        }
                    }
                }
                break;
            }
        }
        
        return nickname;
    }


    public static void updateType(String id, int type) {
        try {
            URL url = new URL("http://192.168.178.56/send_ig.php");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("id_ig", id+"");
            arguments.put("type", type+"");
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            http.connect();

            int responseCode = http.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response);
            } else {
                System.out.println("POST request not worked");
            }

            http.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonNode getJsonFromId(String id){
        ArrayList<String> nickname=new ArrayList<>();
        JsonNode n=null;
        try {
            URL url = new URL("http://192.168.178.56/get_needed_ig.php");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            Map<String,String> arguments = new HashMap<>();
            arguments.put("id_ig", id);
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            http.connect();

            int responseCode = http.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                ObjectMapper mapper=new ObjectMapper();
                JsonNode json=mapper.readTree(mapper.readTree(response.toString()).path("ig_profiles").get(0).get("json_path").asText());
                return json;
            } else {
                System.out.println("POST request not worked");
            }

            http.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    public static boolean isIn(String id) {
        try {
            URL url = new URL("http://192.168.178.56/check_presence.php");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            Map<String,String> arguments = new HashMap<>();
            arguments.put("id_ig", id);
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
            http.connect();

            int responseCode = http.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        http.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response);
                return Boolean.parseBoolean(String.valueOf(response));
            } else {
                System.out.println("POST request not worked");
            }

            http.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
