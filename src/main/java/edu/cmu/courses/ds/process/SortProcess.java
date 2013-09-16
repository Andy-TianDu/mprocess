package edu.cmu.courses.ds.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cmu.courses.ds.io.TransactionalFileInputStream;
import edu.cmu.courses.ds.io.TransactionalFileOutputStream;

/**
 * The sort process example of <code>MigratableProcess</code>
 * Sort characters in each line of a file.
 * This process demonstrates the flexibility of our framework,
 * to stop any at any specific point in a loop and migrate.
 * 
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.process.MigratableProcess
 * @see edu.cmu.courses.ds.io.TransactionalFileInputStream
 * @see edu.cmu.courses.ds.io.TransactionalFileOutputStream
 */
public class SortProcess extends MigratableProcess{

	/**
	 * Which step is the process in.
	 */
	int step;
	
	/**
	 * Record current processing line.
	 */
	String line;
	
	/**
	 * The character array of current processing line.
	 */
	char[] chars;
	TransactionalFileInputStream inputStream;
	TransactionalFileOutputStream outputStream;

    
    /**
     * The constructor with parameters.
     * 
     * @param args command line arguments from </code>ProcessManager
     * <code>
     * @throws Exception if IOException
     */
	public SortProcess(String args[]) throws Exception
	{
		super(args);
		if (args.length < 2) {
			System.out.println("usage: SortProcess <inputFile> <outputFile>");
			return;
		}
        inputStream = new TransactionalFileInputStream(new File(arguments.get(0)));
        outputStream = new TransactionalFileOutputStream(new File(arguments.get(1)));
        
        step = 0;
        line = "";
	}
	

    
    /**
     * Implementation of <code>processing()</code> from
     * <code>MigratableProcess</code>.
     * This function should loop with the <code>suspending</code>
     * and <code>dead</code> flag.
     * First read a line as string, Second convert it to character array,
     * third sort them, fourth convert back to string, finally write the
     * sort result to a file. There are stops(200ms) between each step.
     * The process can resume to a perticular step after migration.
     * 
     * @throws IOException
     */
    @Override
    public void processing() throws IOException {
    	DataInputStream   reader = new DataInputStream(inputStream);
        PrintStream  writer = new PrintStream(outputStream);
        //findStreamField();
        while(!suspending){
        	switch(step) {
        	case 0:
        		line = reader.readLine(); 
            	if(line == null)
            		break;
	            try {
	                Thread.sleep(200);
	            } catch (InterruptedException e) {
	                LOG.error("SortProcess[" + id + "]: interrupted", e);
	            }
    			step++;
    			break;
        	case 1:
        		chars = line.toCharArray();
	            try {
	                Thread.sleep(200);
	            } catch (InterruptedException e) {
	                LOG.error("SortProcess[" + id + "]: interrupted", e);
	            }
        		step++;
        		break;
        	case 2:
        		Arrays.sort(chars);
	            try {
	                Thread.sleep(200);
	            } catch (InterruptedException e) {
	                LOG.error("SortProcess[" + id + "]: interrupted", e);
	            }
        		step++;
        		break;
        	case 3:
        		line = new String(chars);
	            try {
	                Thread.sleep(200);
	            } catch (InterruptedException e) {
	                LOG.error("SortProcess[" + id + "]: interrupted", e);
	            }
        		step++;
        		break;
        	case 4:
        		writer.println(line);
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                LOG.error("SortProcess[" + id + "]: interrupted", e);
	            }
        		step = 0;
        		break;
        	default:
        		LOG.error("SortProcess[" + id + "]: failed");
        		line = null;
        	}
        	if(line == null)
        		break;
        }
        reader.close();
        writer.close();
        inputStream.close();
        outputStream.close();
    }
}
