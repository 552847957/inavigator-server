package ru.sbt.utils.backup.file;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import ru.sbt.utils.backup.command.RestoreCommand;


public class CSVFile implements Closeable{


    private final static CSVFormat FORMAT = CSVFormat.EXCEL.withDelimiter(';').withNullString("NULL");
    private CSVPrinter printer;
    private CSVParser parser;
    private Iterator<CSVRecord> iterator;
    
    public CSVFile(File root, String tableName, String[] header) throws IOException {
    	File tableFile = FileHelper.getFileForTable(root, tableName);
    	if (!FileHelper.createFile(tableFile))
    		throw new IOException("Can't create file "+tableFile.getAbsolutePath()+" to write backup");
		printer = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(tableFile, true), Charset.forName("UTF-8")), FORMAT.withHeader(header));
    }
    
    public CSVFile(File root, String tableName) throws IOException {
    	File tableFile = FileHelper.getFileForTable(root, tableName);
    	if (!tableFile.exists())
			throw new RestoreCommand.BackupNotFound(tableFile.getAbsolutePath());
		Charset cs = Charset.forName("UTF-8");
//		if (tableFile.getName().contains("SYNC_PUSH_NOTIFICATIONS_CLIENTS")) {
//			cs = Charset.forName("WINDOWS-1251");
//		}
    	parser = CSVParser.parse(tableFile,cs, FORMAT);
    	iterator = parser.iterator();
    }
    
    public List<String> getRow() {    	
    	if (iterator.hasNext()) {
        	CSVRecord row = iterator.next();
        	List<String> result = new ArrayList<String>(row.size());
        	for (int i = 0; i < row.size(); i++) {
        		result.add(row.get(i));
        	}
        	return result;
    	} else
    		return null;
    }
    
   
    public void save(Iterable<?>... items) throws IOException {
        
        for (Iterable<?> item: items) {
        	printer.printRecord(item);
        }    	
        
        printer.flush();
    }
    
    public void save(Iterable<?> item) throws IOException {
    	printer.printRecord(item);
        printer.flush();
    }
    
    public void close() throws IOException {
    	if (printer != null)
    		printer.close();
    	if (parser != null) {
    		parser.close();    		
    	}
    }

}