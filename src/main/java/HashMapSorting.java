import java.util.*;
import java.util.Map;
import java.util.Map.Entry;

public class HashMapSorting {
	public static Map<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        
        Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String,Integer>>() { 
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				Integer v1 = o1.getValue(); 
				Integer v2 = o2.getValue(); 
				return v2.compareTo(v1);

		} };

		Collections.sort(list, valueComparator);
    
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	
	
	public static Map<Integer, Integer> sortByValue1(Map<Integer, Integer> map) {
        List<Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        
        Comparator<Entry<Integer, Integer>> valueComparator = new Comparator<Entry<Integer,Integer>>() { 
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				Integer v1 = o1.getValue(); 
				Integer v2 = o2.getValue(); 
				return v2.compareTo(v1);

		} };

		Collections.sort(list, valueComparator);
    
        Map<Integer, Integer> result = new LinkedHashMap<>();
        for (Entry<Integer, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}

