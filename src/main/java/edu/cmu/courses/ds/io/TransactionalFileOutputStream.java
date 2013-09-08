package edu.cmu.courses.ds.io;

import java.io.*;

/**
 * Transactional FileOutputStream
 *
 *
 * @author Jian Fang(jianf)
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable {
    private File targetFile;
    private long offset;
    private transient RandomAccessFile handler;
    private boolean migrated;

    public TransactionalFileOutputStream(File targetFile){
        this.targetFile = targetFile;
        this.offset = 0;
        this.migrated = false;
    }

    @Override
    public void write(int b) throws IOException {
        if(migrated || handler == null){
            handler = new RandomAccessFile(targetFile, "rw");
            handler.seek(offset);
            migrated = false;
        }
        handler.write(b);
        offset++;
    }

    public void setMigrated(boolean migrated){
        this.migrated = migrated;
    }
}
