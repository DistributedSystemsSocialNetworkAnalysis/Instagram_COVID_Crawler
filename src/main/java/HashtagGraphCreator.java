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
	static HashSet<String> fixedHashtags = new HashSet<String>(Arrays.asList(new String[] {"#coronavirus","#covid","#covid19","#covid_19","#quarantine","#quarantena",
	"#quarantinelife","#lockdown","#lockdowndiaries","#stayhome","#socialdistance","#socialdistancing","#pandemic","#pandemic2020", "#andràtuttobene"}));
	static HashMap<String,Integer> occurrences = new HashMap<String,Integer>();
	static File dataDir = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
	static GraphModel graphModel;
	static UndirectedGraph undirectedGraph;
	static Workspace workspace;
	static int ID;


	/* inizializzazione progetto */
	public static void init() throws IOException {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();		
		graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		undirectedGraph = graphModel.getUndirectedGraph();
		ID = 0;
		getOccurrences();
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
										HashSet<String> ht = new HashSet<String>();							
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


	public static void getOccurrences() throws IOException {
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_table.xlsx"));
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);

		// scorro il file per riga	
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			// salto la riga con gli header
			if(row.getRowNum()!=0) { 
				String hashtag = (String) row.getCell(1).getStringCellValue();
				int occ = (int) row.getCell(0).getNumericCellValue();
				occurrences.put(hashtag, occ);
				System.out.println("L'hashtag " + hashtag + " occorre " + occ + " volte.");					
			}
		}
		
		f.close();
		workbook.close();
	}


	/* funzione che filtra gli hashtag non interessanti */
	public static HashSet<String> filterHashtags(JSONArray hashtags) throws IOException {
		HashSet<String> ht = new HashSet<String>();
		     
		for(int h = 0; h < hashtags.size(); h++) {
			// se l'hashtag non fa parte di quelli coinvolti nella ricerca dati
			if(!fixedHashtags.contains((String)hashtags.get(h))) {	
				System.out.println("Non è contenuto in fixedHashtag: " + hashtags.get(h));
				// se occorre più di 1000 volte 
				if(occurrences.containsKey(hashtags.get(h)) && occurrences.get(hashtags.get(h)) >= 1000) {
					System.out.println("AGGIUNTO: " + hashtags.get(h));
					ht.add((String)hashtags.get(h));
				} else System.out.println("Rimuovo l'hashtag: " + hashtags.get(h) + " poiché poco diffuso.");
			}				
			else System.out.println("Rimuovo l'hashtag: " + hashtags.get(h) + " poiché già coinvolto nella ricerca.");
		}

		return ht;
	}


	/* crea i nodi del grafo basandosi sulla lista degli hashtag del post corrente */
	public static void createNodes(HashSet<String> ht) throws IOException {
		Iterator<String> it = ht.iterator();
		while(it.hasNext()) {
			String hashtag = it.next();
			Node n = graphModel.factory().newNode("n" + ID);
			ID++;
			n.setLabel(hashtag);
			
			if(!undirectedGraph.contains(n)) { // controllo che il grafo non contenga già il nodo con que
				undirectedGraph.addNode(n);	
				System.out.println("Aggiunto nodo con etichetta: " + n.getLabel());		
			} else ID--; // non ho inserito il nodo che ho dichiarato sopra, quindi decremento ID
		}
		
		System.out.println(ht.size());
		System.out.println("DIMENSIONE:      " + undirectedGraph.getNodes().toArray().length);
	}


	/* crea gli archi del grafo basandosi sugli hashtag contenuti nel post */
	public static void createEdges(HashSet<String> ht) {
		HashSet<Node> nodes1 = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		Iterator<Node> it1 = nodes1.iterator();
		// scorro i nodi
		while(it1.hasNext()) {	
			Node n1 = it1.next();
			
			HashSet<Node> nodes2 = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
			Iterator<Node> it2 = nodes2.iterator();
			// per ogni nodo scorro la stessa lista dei nodi (ignorando quello uguale) e creo un arco
			while(it2.hasNext()) {
				Node n2 = it2.next();
				if(n1!=n2) {
					Edge e = graphModel.factory().newEdge(n1, n2, 1, false); // TO DO: peso degli archi!
					undirectedGraph.addEdge(e);
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
