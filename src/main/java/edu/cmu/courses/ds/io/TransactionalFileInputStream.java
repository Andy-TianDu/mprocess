package edu.cmu.courses.ds.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

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
 * @see edu.cmu.courses.ds.io.TransactionalFileOutputStream
 * @see java.io.RandomAccessFile
 */
public class TransactionalFileInputStream extends InputStream
        implements Serializable {
    /**
     * The <code>File</code> object of input file
     */
    private File sourceFile;

    /**
     * The current reading offset
     */
    private long offset;

    /**
     * Random access file handler, we use <code>RandomAccessFile</code>
     * to seek to the offset when reading the input file
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
     * Constructor of TransactionalFileInputStream
     *
     * @param sourceFile the input file object
     */
    public TransactionalFileInputStream(File sourceFile) {
        this.sourceFile = sourceFile;
        this.offset = 0;
        this.migrated = false;
    }

    /**
     * Implementation of <code>read()</code>
     * At the beginning, we check the migrated flag, if the flag
     * is set, we reset the file handler and seek the file to the
     * previous offset. Then, we call <code>handler.read()</code>
     * to read one byte. Finally, we increase the <code>offset</code>
     * value.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         file has been reached.
     * @throws IOException if an I/O error occurs. Not thrown if
     *                     end-of-file has been reached.
     * @see java.io.RandomAccessFile#seek(long)
     * @see java.io.RandomAccessFile#read()
     */
    @Override
    public int read() throws IOException {
        if (migrated || handler == null) {
            handler = new RandomAccessFile(sourceFile, "r");
            migrated = false;
            handler.seek(offset);
        }
        int result = handler.read();
        offset++;
        return result;
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
