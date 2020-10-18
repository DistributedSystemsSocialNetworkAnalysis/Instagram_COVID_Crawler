import java.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Stopwords;



public class Lemmatizer {

	protected StanfordCoreNLP pipeline;

	public Lemmatizer() {
		// Create StanfordCoreNLP object properties, with POS tagging
		// (required for lemmatization), and lemmatization
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");

		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution
		this.pipeline = new StanfordCoreNLP(props);
	}

	public List<String> lemmatize(HashMap<String, Integer> tokenOccurrences, String documentText) {
		List<String> lemmas = new LinkedList<String>();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for(CoreMap sentence: sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

				Stopwords sw = new Stopwords();
				sw.add("."); sw.add(":"); sw.add("("); sw.add(")"); sw.add("-"); sw.add("?"); sw.add(",");
				sw.add(".."); sw.add("..."); sw.add("*"); sw.add("??"); sw.add("???"); sw.add("/"); sw.add("+");
				sw.add("_"); sw.add("!"); sw.add("!!"); sw.add("!!!"); sw.add(":-"); sw.add("....."); sw.add("|");
				sw.add("~"); sw.add("•"); sw.add("&"); sw.add("-");

				String s = token.get(LemmaAnnotation.class);
				// Retrieve and add the lemma for each word into the list of lemmas
				if(!s.contains("#") && !s.contains("@") && !s.contains("-") && !sw.is(s) && !isNumber(s)) {
					lemmas.add(s);
					if(s.contains(",")) s.replace(",", "");
					if(s.contains(".")) s.replace(".", "");
					if(tokenOccurrences.containsKey(s))
						tokenOccurrences.put(s, new Integer(tokenOccurrences.get(s)+1));
					else tokenOccurrences.put(s, new Integer(1));
					System.out.println(s);
				}
			}
		}

		return lemmas;
	}
	
	
	public boolean isNumber(String s) {
		if(s.equals("2020")) return false;
		
		try {  
		    Double.parseDouble(s);  
		    return true;
		  } catch(NumberFormatException e){  
		    return false;  
		  }  
	}
}