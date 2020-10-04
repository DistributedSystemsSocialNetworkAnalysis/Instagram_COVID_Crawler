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
	static HashMap<String,String> edges = new HashMap<String,String>();
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
		getOccurrences(); // recupero le occorrenze degli hashtag
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
								for(int m = 0; m < posts.size(); m++) {
									System.out.println(files[j].getName());
									hashtags = new JSONArray();
									hashtags = (JSONArray) ((JSONObject)posts.get(m)).get("Hashtags");									
									if(hashtags!= null && hashtags.size()!=0) {
										HashSet<String> ht = new HashSet<String>();							
										ht = filterHashtags(hashtags);
										createNodes(ht);
										addEdges(ht); // aggiunge gli archi alla struttura dati (hash map edges)
									}								
								}
							}
						}
					}
				}
			}		
		}
				
		writeEdgesOnFile(); // scrive il file edge-list		
		formatGraph(); // formatta il grafo aggiundo i singoli archi
	}
	
	
	/* funzione per completare il grafo: inserisco gli archi precedentemente scritti nel file edge_list_table.xlsx */
	public static void formatGraph() throws IOException {
		System.out.println("Formatting del grafo...");
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\edge_list_table.xlsx"));
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		
		// scorro il file per riga	
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			
			// salto la riga degli header della tabella
			if(row.getRowNum()!=0) {
				String idSource = (String) row.getCell(0).getStringCellValue(); 
				String idDest = (String) row.getCell(1).getStringCellValue();			
				Node source = undirectedGraph.getNode(idSource);
				Node dest = undirectedGraph.getNode(idDest);
				Edge e = graphModel.factory().newEdge(source, dest, 1, false); // TO DO: peso degli archi!
				undirectedGraph.addEdge(e);
			} 
		}
		
		System.out.println("Grafo degli hashtag creato con successo.");
	}


	/* funzione che estrae le occorrenze degli hashtag dal file hashtags_table e le inserisce in una HashMap*/
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
				System.err.println("L'hashtag " + hashtag + " occorre " + occ + " volte.");					
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
				//System.out.println("Non è contenuto in fixedHashtag: " + hashtags.get(h));
				// se occorre più di 1000 volte 
				if(occurrences.containsKey(hashtags.get(h)) && occurrences.get(hashtags.get(h)) >= 1000) {
					//System.out.println("AGGIUNTO: " + hashtags.get(h));
					ht.add((String)hashtags.get(h));
				} //else System.out.println("Rimuovo l'hashtag: " + hashtags.get(h) + " poiché poco diffuso.");
			}				
			//else System.out.println("Rimuovo l'hashtag: " + hashtags.get(h) + " poiché già coinvolto nella ricerca.");
		}

		return ht;
	}
	
	
	public static boolean isPresent(HashSet<Node> nodes, Node n) {	
		for(Node n1: nodes) {
			if(n1.getLabel().equals(n.getLabel())) 
				return true;
		}		
		return false;
	}


	/* crea i nodi del grafo basandosi sulla lista degli hashtag del post corrente */
	public static void createNodes(HashSet<String> ht) throws IOException {
		Iterator<String> it = ht.iterator();
		HashSet<Node> nodes = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		while(it.hasNext()) {
			String hashtag = it.next();
			Node n = graphModel.factory().newNode("n" + ID);
			ID++;
			n.setLabel(hashtag);
			
			if(!isPresent(nodes,n)) { // controllo che il grafo non contenga già il nodo con que
				undirectedGraph.addNode(n);	
				System.out.println("Aggiunto nodo con etichetta: " + n.getLabel());		
			} else {
				System.err.println("NODO GIA' PRESENTE NEL GRAFO: " + n.getLabel());
				ID--; // non ho inserito il nodo che ho dichiarato sopra, quindi decremento ID
			}
		}
		
		System.out.println(ht.size());
		System.out.println("DIMENSIONE:      " + undirectedGraph.getNodes().toArray().length);
	}


	/* crea gli archi del grafo basandosi sugli hashtag contenuti nel post */
	public static void addEdges(HashSet<String> ht) throws IOException {	
		HashSet<Node> nodes = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		Iterator<Node> it1 = nodes.iterator();
		// scorro i nodi
		while(it1.hasNext()) {	
			Node n1 = it1.next();
			Iterator<Node> it2 = nodes.iterator();
			// per ogni nodo scorro la stessa lista dei nodi (ignorando quello uguale) e creo un arco
			while(it2.hasNext()) {
				Node n2 = it2.next();
				if(n1.getId()!=n2.getId()) {				
					edges.put((String)n1.getId(), (String)n2.getId());
					System.out.println(n1.getLabel() + " - " + n2.getLabel());
				}
			}				
		}	
	}
	
	
	/* scrive il file edge_list */
	public static void writeEdgesOnFile() throws IOException {
		TableFormatter formatter = new TableFormatter("edge_list", new String[]{"id source","id destination"});
		formatter.fillTable2(edges);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(formatter.getFile()));
		formatter.workbook.write(out); // scrivo gli archi su file
		formatter.workbook.close();
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
		
		System.out.println("GRAFO: nodi = " + undirectedGraph.getNodeCount() + ", archi = " + undirectedGraph.getEdgeCount());

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
