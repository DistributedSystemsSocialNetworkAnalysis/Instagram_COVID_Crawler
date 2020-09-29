import java.io.*;
import java.util.HashMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


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

	
	public void fillTable(HashMap<String, Integer> map1) {
		int i = 1;
		System.out.println("Riempio la tabella excel");
		for(String key: map1.keySet()) {
			int val  = ((Integer)map1.get(key)).intValue();
			if(val>=100) {
				Row row = sheet.createRow(i); // creo una riga
				Cell cell1 = row.createCell(0); // prima cella della riga appena creata	
				cell1.setCellValue(val);
				
				cell1 = row.createCell(1);
				cell1.setCellValue(key);
	
				System.out.println("Ho inserito il record: " + val + ", " + key);
				i++;
			}
		}
		
		writeTable();		
	}
	
	
	public void writeTable() {
		try {
			FileOutputStream outputStream = new FileOutputStream(excel);
			System.out.println("Scrivo la tabella su file:");
			workbook.write(outputStream);
			workbook.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
