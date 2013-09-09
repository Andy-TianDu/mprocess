package edu.cmu.courses.ds.process;


import edu.cmu.courses.ds.io.TransactionalFileInputStream;
import edu.cmu.courses.ds.io.TransactionalFileOutputStream;

import java.io.*;

/**
 * The grep process example of <code>MigratableProcess</code>
 * Search in the input file, if the line of input file contains
 * a specific string, output the line to the output file.
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.process.MigratableProcess
 * @see edu.cmu.courses.ds.io.TransactionalFileInputStream
 * @see edu.cmu.courses.ds.io.TransactionalFileOutputStream
 * @see java.io.BufferedReader
 * @see java.io.BufferedWriter
 */
public class GrepProcess extends MigratableProcess {

    /**
     * Default constructor
     */
    public GrepProcess(){
        super();
    }

    /**
     * Constructor with process arguments
     * @param args process arguments
     */
    public GrepProcess(String[] args){
        super(args);
    }

    /**
     * Implementation of <code>processing()</code> from
     * <code>MigratableProcess</code>.
     * This function should loop with the <code>suspending</code>
     * and <code>dead</code> flag.
     * 
     *
     * @throws IOException if any IO error occurs.
     */
    @Override
    public void processing() throws IOException {
        if(arguments.size() < 3){
            System.out.println("GrepProcess[" + id + "]: " +
                               "usage: run GrepProcess QUERY INPUT OUTPUT");
            return;
        }
        String query = arguments.get(0);

        inputStream = new TransactionalFileInputStream(new File(arguments.get(1)));
        outputStream = new TransactionalFileOutputStream(new File(arguments.get(2)));

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        String line = "";

       
        while(!dead) {
	        while(!suspending){
	            line = reader.readLine();
	            if(line == null)break;
	            if(line.contains(query)){
	                writer.write(line + "\n");
	                writer.flush();
	            }
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                LOG.error("GrepProcess[" + id + "]: interrupted", e);
	            }
	        }
            if(line == null)break;
            suspended = true;
    	}
        reader.close();
        writer.close();

        inputStream.close();
        outputStream.close();
    }
}
