package ru.sberbank.syncserver2.service.file.fragments;

import java.io.File;

import ru.sberbank.syncserver2.util.FileCopyHelper;

public class FileFragmentsTransportTest {
	
	public static void main(String args[]) throws Exception {
		
		File root = new File("C:\\Users\\sbt-gordienko-mv\\mgordienko\\_TEMP\\2\\input");
		File inputFile = new File(root, "MIS_PROGNOZ_SB_DATA.sqlite");
		File outputDirectory = new File(root,"generated"); 
		File archiveDirectory = new File(root,"archive"); 
		File tempDirectory = new File(root,"temp");
		
		while(true) {
			
			FileCopyHelper.reliableDeleteFolderAndSubFolders(outputDirectory);
			FileCopyHelper.reliableDeleteFolderAndSubFolders(archiveDirectory);
			FileCopyHelper.reliableDeleteFolderAndSubFolders(tempDirectory);
			outputDirectory.mkdir();
			archiveDirectory.mkdir();
			tempDirectory.mkdir();
			
			// 1 этап - генерируем файлы с фрагментами
			FileFragmentsTransportHelper.divideIntpufileIntoFragments(inputFile, outputDirectory, 100);
			
			//2 этап сохранение в архив
			File[] fragmentLists = outputDirectory.listFiles();
			for(File f: fragmentLists) {
				FileFragmentsTransportHelper.addedFileFragmentToArchive(f, archiveDirectory);
			}
			
			// этап 3 собираем файлы с фрагментами
			for(File f: fragmentLists) {
				FileFragmentsTransportHelper.collectAllFragmentsAndCreateSourceFile(f,tempDirectory);
			}
		}
	}
	
}
