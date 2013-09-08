package edu.cmu.courses.ds.io;

import java.io.*;

/**
 * Transactional FileInputStream
 *
 * The difference between transactional stream and normal
 * steam is, transactional stream needs to remember its IO
 * state, in order to support IO handling suspend and resume.
 * In our implementation of transactional stream, we use
 * <code>offset</code> to save the IO state, and we use
 * <code>RandomAccessFile</code> to seek the offset before
 * reading or writing.
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.io.TransactionalFileInputStream
 * @see java.io.RandomAccessFile
 */
public class TransactionalFileOutputStream extends OutputStream
        implements Serializable {
    /**
     * The <code>File</code> object of output file
     */
    private File targetFile;

    /**
     * The current writing offset
     */
    private long offset;

    /**
     * Random access file handler, we use <code>RandomAccessFile</code>
     * to seek to the offset when writing the output file
     * Note: this field is <code>transient</code>, which means we don't
     * need to serialize this field.
     */
    private transient RandomAccessFile handler;

    /**
     * The migrated flag. When the flag is set, we should reset the
     * file handler and seek to the previous offset
     */
    private boolean migrated;

    /**
     * Constructor of TransactionalFileOutputStream
     *
     * @param targetFile the output file object
     */
    public TransactionalFileOutputStream(File targetFile) {
        this.targetFile = targetFile;
        this.offset = 0;
        this.migrated = false;
    }

    /**
     * Implementation of <code>write()</code>
     * At the beginning, we check the migrated flag, if the flag
     * is set, we reset the file handler and seek the file to the
     * previous offset. Then, we call <code>handler.write()</code>
     * to write one byte. Finally, we increase the <code>offset</code>
     * value.
     * @param b the <code>byte</code> to be written.
     * @throws IOException if an I/O error occurs.
     * @see java.io.RandomAccessFile#seek(long)
     * @see java.io.RandomAccessFile#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        if (migrated || handler == null) {
            handler = new RandomAccessFile(targetFile, "rw");
            handler.seek(offset);
            migrated = false;
        }
        handler.write(b);
        offset++;
    }

    /**
     * Set the migrated flag
     *
     * @param migrated the migrated value
     */
    public void setMigrated(boolean migrated) {
        this.migrated = migrated;
    }
}
