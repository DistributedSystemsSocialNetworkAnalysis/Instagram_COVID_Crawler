import java.io.*;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;


public class TableFormatter {
	public Workbook workbook;
	public Sheet sheet;
	public File excel;
	
	
	public TableFormatter(String nameOfTable, String[] headers) throws IOException {
		excel = new File("C:\\Users\\marti\\git\\TirocinioProtano\\statistics\\" + nameOfTable + "_table.xlsx");
		if(!excel.exists())
			excel.createNewFile();
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet(nameOfTable);
		sheet.setColumnWidth(0, 4000);
		sheet.setColumnWidth(1, 6000);
		Row header = sheet.createRow(0); // creo la prima riga che conterrà gli header
		for(int i = 0; i<headers.length; i++) {	
			Cell headerCell = header.createCell(i);
			headerCell.setCellValue(headers[i]);
		}
	}

	
	public void fillTable1(HashMap<String, Integer> map1) {
		int i = 1;
		System.out.println("Riempio la tabella excel");
		for(String key: map1.keySet()) {
			try {
				int val  = ((Integer)map1.get(key)).intValue();
				val = (int)Math.log(val);
				//if(val>=100) {
					Row row = sheet.createRow(i); // creo una riga
					Cell cell1 = row.createCell(0); // prima cella della riga appena creata	
					cell1.setCellValue(val);
					
					cell1 = row.createCell(1);
					cell1.setCellValue(key);
		
					System.out.println("Ho inserito il record: " + val + ", " + key);
					i++;
				//}
			} catch(IllegalArgumentException e) {
				break;
			}
		}
		
		writeTable();		
	}
	
	
	public void fillTable2(UndirectedGraph undirectedGraph, HashMap<String,String> edges) {
		int i = 1;
		for(String idSource: edges.keySet()) {
			String idDest  = edges.get(idSource);
			
			Row row = sheet.createRow(i); // creo una riga
			Cell cell1 = row.createCell(0); // prima cella = id sorgente
			cell1.setCellValue(idSource);
				
			cell1 = row.createCell(1); // seconda cella = id destinazione
			cell1.setCellValue(idDest);
			
			Node n1 = undirectedGraph.getNode(idSource);
			Node n2 = undirectedGraph.getNode(idDest);
	
			try {
			System.out.println("Ho inserito il record: " + "(" + n1.getLabel() + " , " + idSource+ ")" + " - " + "(" + n2.getLabel() + " , " + idDest + ")");
			} catch(NullPointerException e) {
				System.err.println(n1);
				System.err.println(n2);
			}
			
			i++;	
		}
	}
	
	
	public File getFile() {
		return this.excel;
	}
	

	public void writeTable() {
		try {
			FileOutputStream outputStream = new FileOutputStream(excel);
			System.out.println("Scrivo la tabella su file.");
			workbook.write(outputStream);
			workbook.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
