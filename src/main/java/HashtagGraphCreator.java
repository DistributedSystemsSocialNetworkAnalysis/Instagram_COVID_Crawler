import org.openide.util.*;
import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.*;
import org.gephi.project.api.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;


public class HashtagGraphCreator {
	static ArrayList<String> fixedHashtags = new ArrayList<String>(Arrays.asList(new String[] {"#coronavirus","#covid","#covid19","#covid_19","#quarantine","#quarantena",
	"#quarantinelife","#lockdown","#lockdowndiaries","#stayhome","#socialdistance","#socialdistancing","#pandemic","#pandemic2020", "#andràtuttobene"}));	
	static File dataDir = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
	static GraphModel graphModel;
	static UndirectedGraph undirectedGraph;
	static Workspace workspace;
	static int ID;

	
	/* inizializzazione progetto */
	public static void init() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();		
		graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		undirectedGraph = graphModel.getUndirectedGraph();
		ID = 0;
	}
	
	
	/* creo il grafo degli hashtags */
	public static void createGraph() throws IOException {
		if(dataDir.isDirectory()) {
			File[] directories = dataDir.listFiles();
			
			/* scorre le cartelle con date (giorno-mese-anno) */
			for(int i=0; i < directories.length; i++) {
				System.out.println("Entro nella cartella: " + directories[i].getName());
				
				if(directories[i].isDirectory()) {
					File[] f = directories[i].listFiles();
					
					/* scorro le cartelle degli hashtag*/
					for(int k=0; k < f.length; k++) {
						System.out.println("Entro nella cartella: " + f[k].getName());
									
						File[] files = f[k].listFiles();					
						/* scorro i singoli file */
						for(int j=0; j<files.length; j++) {
							
							/* considero solo i file filtrati */
							if(files[j].getName().contains("filtered")) {
								/* parso il file */
								Reader reader = null;
								JSONArray posts = null;
								try {
								    reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[j]), "utf-8"));
								    JSONParser parser = new JSONParser();
								    posts = (JSONArray) parser.parse(reader);	
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								
								JSONArray hashtags = new JSONArray();	
								/* scorro i post nel file */
								for(int m=0; m<posts.size(); m++) {
									hashtags = new JSONArray();
									hashtags = (JSONArray) ((JSONObject)posts.get(m)).get("Hashtags");									
									if(hashtags!= null && hashtags.size()!=0) {
										ArrayList<String> ht = new ArrayList<String>();									
										ht = filterHashtags(hashtags);
										createNodes(ht);
										createEdges(ht);
										
									}								
								}
								
							}
							
							
						}
						
					}
					
				}
				
			}		
			
		}
	}
	
	
	public static int occurrences(String hashtag) throws IOException {
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_table.xlsx"));
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		int occ = 0;
		 
		//Iterate through each rows one by one
		 Iterator<Row> rowIterator = sheet.iterator();
		 while (rowIterator.hasNext()) {
			 Row row = rowIterator.next();
			 
			 if(row.getRowNum()!=0) {		
			     if(((String)row.getCell(1).getStringCellValue()).equals(hashtag)) {
			    	 occ = (int) row.getCell(0).getNumericCellValue();
			    	 System.out.println("L'hashtag " + hashtag + " occorre " + occ + " volte.");
			    	 break;
			     }		    
			 }
		 }
		 
		 return occ;
	}
	
	
	public static ArrayList<String> filterHashtags(JSONArray hashtags) throws IOException {
		ArrayList<String> ht = new ArrayList<String>();
		for(int h=0; h<hashtags.size(); h++) {
			/*
			if(fixedHashtags.contains((String)hashtags.get(h))) {
				System.out.println("Rimuovo l'hashtag: " + hashtags.get(h));
				hashtags.remove(h);			
			} else if(occurrences((String)hashtags.get(h)) < 500) {
				
				hashtags.remove(h);
			} */
			
			if(!fixedHashtags.contains((String)hashtags.get(h))) {	
				System.out.println("Non è contenuto in fixedHashtag: " + hashtags.get(h));
				if(occurrences((String)hashtags.get(h)) >= 500) {
					System.out.println("AGGIUNTO: " + hashtags.get(h));
					ht.add((String)hashtags.get(h));
				}
			}				
			else System.out.println("Rimuovo l'hashtag: " + hashtags.get(h));
		}

		return ht;
	}

	
	public static void createNodes(ArrayList<String> ht) throws IOException {
		for(int h=0; h<ht.size(); h++) {
			Node n = graphModel.factory().newNode("n" + ID);
			ID++;
			n.setLabel((String) ht.get(h));
			if(!undirectedGraph.contains(n))
				undirectedGraph.addNode(n);	
			System.out.println("Aggiunto nodo con etichetta: " + n.getLabel());					
		}
		System.out.println(ht.size());
		System.out.println("DIMENSIONE:      " + undirectedGraph.getNodes().toArray().length);
	}
	
	
	/* hashtags = hashtags di un post */
	public static void createEdges(ArrayList<String> ht) {
		Node[] nodes1 = undirectedGraph.getNodes().toArray();
		for(int i = 0; i<nodes1.length; i++) {	
			Node n1 = nodes1[i];
			Node[] nodes2 = undirectedGraph.getNodes().toArray();
			for(int j = 0; j<nodes2.length; j++) {
				Node n2 = nodes2[j];
				if(n1!=n2) {
					Edge e = graphModel.factory().newEdge(n1, n2, 1, false);
					undirectedGraph.addEdge(e);
					//System.out.println("Aggiunto arco: " + n1.getLabel() + " -> " + n2.getLabel());
				}
			}				
		}
	}
	
	
	/* esporta il grafo su un file */
	public static void exportGraph() {
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		File graphFile = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistic\\hashtags_graph.gexf");
		try {
		    ec.exportFile(graphFile);
		} catch (IOException ex) {
		    ex.printStackTrace();
		    return;
		}
		
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		
		Iterator<Node> it1 = undirectedGraph.getNodes().iterator();
		while(it1.hasNext()) {
			graph.addNode(it1.next());
		}
		
		Iterator<Edge> it2 = undirectedGraph.getEdges().iterator();
		while(it2.hasNext()) {
			graph.addEdge(it2.next());
		}
		
		System.out.println("GRAFO DIRETTO: nodi = " + undirectedGraph.getNodeCount() + ", archi = " + undirectedGraph.getEdgeCount());
		System.out.println("GRAFO INDIRETTO: nodi = " + graph.getNodeCount() + ", archi = " + graph.getEdgeCount());
		
		Exporter exporterGraphML = ec.getExporter("graphml");     //Get GraphML exporter
		exporterGraphML.setWorkspace(workspace);
		StringWriter stringWriter = new StringWriter();
		ec.exportWriter(stringWriter, (CharacterExporter) exporterGraphML);
		
	}
	
	
	public static void main(String[] args) throws IOException {
		HashtagGraphCreator.init();
		HashtagGraphCreator.createGraph();
		HashtagGraphCreator.exportGraph();
	}
}
