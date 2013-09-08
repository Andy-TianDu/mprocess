package edu.cmu.courses.ds.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * Transactional FileInputStream
 *
 *
 * @author Jian Fang(jianf)
 */
public class TransactionalFileInputStream extends InputStream implements Serializable{
    private File sourceFile;
    private long offset;
    private transient RandomAccessFile handler;
    private boolean migrated;

    public TransactionalFileInputStream(File sourceFile){
        this.sourceFile = sourceFile;
        this.offset = 0;
        this.migrated = false;
    }

    @Override
    public int read() throws IOException {
        if(migrated || handler == null){
            handler = new RandomAccessFile(sourceFile, "r");
            migrated = false;
            handler.seek(offset);
        }
        int result = handler.read();
        offset++;
        return result;
    }

    public void setMigrated(boolean migrated){
        this.migrated = migrated;
    }
}
