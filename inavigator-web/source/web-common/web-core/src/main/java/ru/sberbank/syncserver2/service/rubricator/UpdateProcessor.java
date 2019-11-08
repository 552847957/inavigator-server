package ru.sberbank.syncserver2.service.rubricator;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vybubnov on 25.10.13.
 */
public class UpdateProcessor {
    private static final Logger logger = Logger.getLogger(UpdateProcessor.class);

    public void processDelete(String fileName) {
        logger.debug("Deleting file " + fileName);
        File f = new File(fileName);
        f.delete();
    }

    public void processUpdate(String fileName, ResultSet resultSet) throws IOException {
        logger.debug("Create file " + fileName);
        File f = new File(fileName);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        try {
            logger.debug("Start writing blob to file " + fileName);
            writeBlobTofile(f, resultSet);
            logger.debug("Finished writing blob to file " + fileName);
        } catch (SQLException e) {
            logger.error("Error writing blob to file", e);
        }
    }

    private void writeBlobTofile(File f, ResultSet resultSet) throws SQLException, IOException {
        Blob blob = resultSet.getBlob(DatabaseFileReader.COL_DATA);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        InputStream is = new BufferedInputStream(blob.getBinaryStream());
        try {
            IOUtils.copyLarge(is, os, new byte[1024]);
            os.flush();
        } finally {
            is.close();
            os.close();
        }
    }
}
