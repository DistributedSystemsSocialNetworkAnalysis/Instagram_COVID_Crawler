import org.openide.util.*;
import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.*;
import org.gephi.project.api.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.gephi.statistics.plugin.*;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.data.attributes.*;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;



/**
 * 
 * Classe che ha lo scopo di costruire un grafo a partire dagli hashtag dei post di Instagram raccolti.
 * Ad ogni nodo corrisponde un hashtag e a ogni arco tra due nodi la presenza di questi nello stesso post.
 * Il peso assegnato agli archi si riferisce al numero di volte in cui i due hashtag collegati sono apparsi
 * nello stesso post. 
 * Il grafo viene esportato come file nel formato .gexf. (formato facilmente elaborabile tramite Gephi)
 * 
 * 
 * @author Martina Protano
 *
 */
public class HashtagGraphCreator {
	static HashSet<String> fixedHashtags = new HashSet<String>(Arrays.asList(new String[] {"#coronavirus","#covid","#covid19","#covid_19","#quarantine",
	"#quarantena","#quarantinelife","#lockdown","#lockdowndiaries","#stayhome","#socialdistance","#socialdistancing","#pandemic","#pandemic2020", "#andràtuttobene"}));
	static HashMap<String,Integer> occurrences = new HashMap<String,Integer>();
	static HashMap<String,String> edges = new HashMap<String,String>();
	static File dataDir = new File("C:\\Users\\marti\\git\\TirocinioProtano\\data");
	static GraphModel graphModel;
	static UndirectedGraph undirectedGraph;
	static Workspace workspace;
	static int ID; // campo id per i nodi


	/* inizializzazione progetto */
	public static void init() throws IOException {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		workspace = pc.getCurrentWorkspace();		
		graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		undirectedGraph = graphModel.getUndirectedGraph();
		ID = 0;
		//getOccurrences(); // recupero le occorrenze degli hashtag
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
										HashSet<Node> nodes = new HashSet<Node>(); 
										ht = filterHashtags(hashtags);
										System.out.println("Hashtag filtrati. Creo i nodi...");
										nodes = createNodes(ht);
										System.out.println("Creati i nodi associati al post.");
										addEdges(nodes); // aggiunge gli archi alla struttura dati (hash map edges)
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
	
				//if(source!=null && dest !=null) {
					Edge e1 = undirectedGraph.getEdge(source, dest);
					if(e1!=null) { // l'arco è già presente tra questi due nodi, aumento il peso
						e1.setWeight(e1.getWeight()+1);
					} else {
						Edge e = graphModel.factory().newEdge(source, dest, 1, false); // TO DO: peso degli archi!					
						undirectedGraph.addEdge(e);
					}				
				//}
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
				try {
					String hashtag = (String) row.getCell(1).getStringCellValue();
					int occ = (int) row.getCell(0).getNumericCellValue();
					occurrences.put(hashtag, occ);
					System.err.println("L'hashtag " + hashtag + " occorre " + occ + " volte.");		
				} catch(NullPointerException e) {
					System.out.println("(NullPointerException)");
				}
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
				if(occurrences.containsKey(hashtags.get(h)) && occurrences.get(hashtags.get(h)) >= 12) {
					System.out.println("AGGIUNTO: " + hashtags.get(h));
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
	
	
	public static String getID(Node n) {
		HashSet<Node> graphNodes = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		Iterator<Node> it = graphNodes.iterator();

		while(it.hasNext()) {
			Node n1 = it.next();
			if(n1.getLabel().equals(n.getLabel())) 
				return (String) n1.getId();		
		}		
		return null;
	}


	/* crea i nodi del grafo basandosi sulla lista degli hashtag del post corrente */
	public static HashSet<Node> createNodes(HashSet<String> ht) throws IOException {
		Iterator<String> it = ht.iterator();
		HashSet<Node> nodes = new HashSet<Node>(); // struttura dati che tiene i nodi associati agli hashtag del post corrente
		HashSet<Node> graphNodes = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		while(it.hasNext()) {
			String hashtag = it.next();
			Node n = graphModel.factory().newNode("n" + ID);
			ID++;
			n.setLabel(hashtag);
			
			if(!isPresent(graphNodes,n)) { // controllo che il grafo non contenga già il nodo con que
				undirectedGraph.addNode(n);	
				nodes.add(n);
				System.out.println("Aggiunto nodo con etichetta: " + n.getLabel() + " , id = " + ID);		
			} else {
				System.err.println("NODO GIA' PRESENTE NEL GRAFO: " + n.getLabel());
				String id = getID(n);
				Node n1 = graphModel.factory().newNode(id);	
				n1.setLabel(hashtag);
				nodes.add(n1); // aggiungo il nodo all'insieme nodes ma dopo aver recuperato e impostato il suo id originario (è già nel grafo con un certo id) 
				ID--; // non ho inserito il nodo che ho dichiarato sopra, quindi decremento ID
			}
		}
		
		System.out.println(ht.size());
		System.out.println("DIMENSIONE:      " + undirectedGraph.getNodes().toArray().length);
		
		return nodes;
	}


	/* crea gli archi del grafo basandosi sugli hashtag contenuti nel post */
	public static void addEdges(HashSet<Node> nodes) throws IOException {	
		//HashSet<Node> nodes1 = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
		Iterator<Node> it1 = nodes.iterator(); 
		// scorro i nodi
		while(it1.hasNext()) {	
			Node n1 = it1.next();
			//HashSet<Node> nodes2 = new HashSet<Node> (Arrays.asList(undirectedGraph.getNodes().toArray()));
			Iterator<Node> it2 = nodes.iterator();
			// per ogni nodo scorro la stessa lista dei nodi (ignorando quello uguale) e creo un arco
			while(it2.hasNext()) {
				Node n2 = it2.next();
				if(n1.getId()!=n2.getId()) {				
					edges.put((String)n1.getId(), (String)n2.getId());
					//System.out.println(n1.getLabel() + " - " + n2.getLabel());
				}
			}				
		}	
	}
	
	
	/* scrive il file edge_list */
	public static void writeEdgesOnFile() throws IOException {
		TableFormatter formatter = new TableFormatter("edge_list", new String[]{"id source","id destination"});
		formatter.fillTable2(undirectedGraph,edges);
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(formatter.getFile()));
		formatter.workbook.write(out); // scrivo gli archi su file
		formatter.workbook.close();
	}


	/* esporta il grafo su un file */
	public static void exportGraph() {
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		
		try {
			ec.exportFile(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_graph.gexf"));
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
	
	
	public static void analyze() throws IOException {
		Percentile p = new Percentile(0.25);
		FileInputStream f = new FileInputStream(new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_table.xlsx"));
		XSSFWorkbook workbook = new XSSFWorkbook(f);
		XSSFSheet sheet = workbook.getSheetAt(0);
		ArrayList<Double> values = new ArrayList<Double>();
		double sum = 0;
		double media = 0;
		
		// scorro il file per riga	
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			// salto la riga con gli header
			if(row.getRowNum()!=0) { 
				System.out.println(row.getRowNum());
				//String hashtag = (String) row.getCell(1).getStringCellValue();
				try {
					Double occ = (Double) row.getCell(0).getNumericCellValue();
					sum = sum + occ;
					values.add(occ);
				} catch(NullPointerException e) { }
				//occurrences.put(hashtag, occ);
				//System.err.println("L'hashtag " + hashtag + " occorre " + occ + " volte.");					
			}
		}
		
		 double[] val = new double[values.size()];
		 for (int i = 0; i < val.length; i++) {
		    val[i] = values.get(i).doubleValue(); 
		 }
		
		
		double res = p.evaluate(val,25);
		media = sum/1048575;
		
		System.out.println("25th percentile: " + res);
		System.out.println("50th percentile: " + p.evaluate(val,50));
		System.out.println("75th percentile: " + p.evaluate(val,75));
		System.out.println("Media: " + media);
		
		p.setQuantile(10);
		for(int i = 10; i <=100; i+=10) {
			System.out.println(i+"th percentile: " + p.evaluate(val,i));
		}
				
		f.close();
		workbook.close();
		
	}
	
	public static void statistics() {
		//Import file
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		Container container;
		try {
		 File file = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\hashtags_graph.gexf");
		 container = importController.importFile(file);
		 container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED); 
		 //container.setAllowAutoNode(false); //Don’t create missing nodes
		} catch (Exception ex) {
		 ex.printStackTrace();
		 return;
		}
		//Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);
		
		GraphDistance gd = new GraphDistance();
		gd.setDirected(false);		
		gd.execute(graphModel);
		AttributeModel attributeModel = workspace.getLookup().lookup(AttributeController.class).getModel();

		//Get Centrality column created
		AttributeColumn col = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		//Iterate over values
		for (Node n : undirectedGraph.getNodes()) {
			Object[] obj = n.getAttributes();
			for(int j = 0; j<obj.length; j++) 
				System.out.println(obj[j]);
			/*
			Element el = n.getAttribute(col);
			Double centrality = 
			System.out.println(centrality); */
		}
		
	}


	public static void main(String[] args) throws IOException {
		HashtagGraphCreator.init();
		//HashtagGraphCreator.createGraph();
		//HashtagGraphCreator.exportGraph();
		
		//HashtagGraphCreator.analyze();
		HashtagGraphCreator.statistics();
	}
}
