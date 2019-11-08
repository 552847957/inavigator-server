package ru.sberbank.syncserver2.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import org.apache.log4j.Logger;

/**
 * @author Sergey Erin
 *
 */
public class FileLockHelper {

    private static Logger log = Logger.getLogger(FileLockHelper.class);

    public static final String LOCK_EXT = ".lck";

    private File lockedDirectory;
    private String lockName;

    private FileLock lock;
    private FileOutputStream lockFile;
    private FileChannel lockChannel;

    public FileLockHelper(File path, String lockName) {
        this.lockedDirectory = path;
        this.lockName = lockName;
    }

    /**
     * Obtains locks
     *
     * @return true if lock was successful
     */
    public boolean lock() {
        if (lock != null) {
            log.trace("Duplicate lock file attempt");
            return lock.isValid();
        }

        String directoryPath = null;
        try {
            directoryPath = getDirectoryPath();
        } catch (IOException e) {
            log.trace(e.getMessage(), e);
        }

        if (directoryPath == null) {
            log.error("Lock could not be obtained since path is unknown");
            return false;
        }

        try {
            lockFile = new FileOutputStream(new File(directoryPath, lockName).getCanonicalFile());
            lockChannel = lockFile.getChannel();
            lock = lockChannel.lock();
            log.trace("Checking lock validity, status = " + lock.isValid());
        } catch (FileNotFoundException e) {
            log.trace("Lock file failed: '" + e.getMessage());
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        } catch (IOException e) {
            log.trace("Lock file failed: '" + e.getMessage());
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        } catch (OverlappingFileLockException e) {
            log.trace("Lock file failed: '" + e.getMessage());
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        }

        return lock != null? lock.isValid() : false;
    }

    protected final String getDirectoryPath() throws IOException {
        if (lockedDirectory == null) {
            log.trace("Absolute directory path could not be obtained since path is null");
            return null;
        }

        String canonicalPath = lockedDirectory.getCanonicalPath();
        if (lockedDirectory.isFile()) {
            log.trace("Invalid initialization params. Directory is expected as a File parameter");
            return null;
        }

        return canonicalPath;
    }

    /**
     * Releases lock
     */
    public void unlock() {
        if (lock == null) {
            return;
        }

        try {
            lock.release();
        } catch (IOException e) {
            log.trace("Release lock file failed: '" + e.getMessage() + "'");
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        }

        try {
            lockChannel.close();
        } catch (IOException e) {
            log.trace("Lock file channel close failed: '" + e.getMessage() + "'");
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        }

        try {
            lockFile.close();
        } catch (IOException e) {
            log.trace("Lock file close failed: '" + e.getMessage() + "'");
            if (log.isTraceEnabled()) {
                log.trace(e.getMessage(), e);
            }
        }

        boolean deleteResult = new File(lockedDirectory, lockName).delete();
        log.trace("Deleting lock file '" + lockName + "' result: " + (deleteResult? "deleted" : "not deleted"));
    }
}
