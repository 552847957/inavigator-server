package ru.sberbank.syncserver2.service.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVImpl implements GeneratorLogFile {
	private String filename = "Logs.csv";
	
	public CSVImpl() {	}
	
	public CSVImpl(String filename) {
		this.filename = filename;
		
	}

	@Override
	public void generateFile(HttpServletResponse response, List<? extends Iterable<String>> list) {
		response.setContentType("text/csv; charset=Windows-1251");
		response.setHeader("content-disposition", "attachment; filename="+filename);
		response.setHeader("Content-Type", "text/csv");
		response.setHeader("Pragma", "cache");
		response.setHeader("Cache-Control", "private");

        PrintWriter out = null;
        try {
            out = response.getWriter();            
            if (out != null) {       
            	CSVPrinter printer = new CSVPrinter(out, CSVFormat.EXCEL.withDelimiter(';'));
            	for (int i=0;i<list.size();i++) {
            		printer.printRecord((Iterable)list.get(i));            		
            	}
            	printer.flush();
            	printer.close();
            	out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } 
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}	

}
