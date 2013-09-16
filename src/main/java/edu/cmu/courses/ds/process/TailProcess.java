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
 * The tail process example of <code>MigratableProcess</code>
 * Write the last few lines of a file to the output file.
 * This process demonstrates the flexibility of our framework,
 * to open and close multiple times in a process.
 * 
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.process.MigratableProcess
 * @see edu.cmu.courses.ds.io.TransactionalFileInputStream
 * @see edu.cmu.courses.ds.io.TransactionalFileOutputStream
 */
public class TailProcess extends MigratableProcess{
	
	/**
	 * Which step is the process in.
	 */
	private int step;
	
	/**
	 * the number of lines in the input file
	 */
    private int fileLine;
    
    /**
     * the current line being processed
     */
    private int lineCount;
    
    /**
     * the number of lines to output 
     */
    private int outputLine;
    
	TransactionalFileInputStream inputStream;
	
	TransactionalFileOutputStream outputStream;
	

    /**
     * The constructor with parameters.
     * 
     * @param args command line arguments from </code>ProcessManager
     * <code>
     * @throws Exception if IOException
     */
    public TailProcess(String[] args){
        super(args);
        if(arguments.size() < 3){
            System.out.println("GrepProcess[" + id + "]: " +
                               "usage: run TailProcess OUTPUTLINE INPUT OUTPUT");
            return;
        }
        outputLine = Integer.parseInt(arguments.get(0));
        
        inputStream = new TransactionalFileInputStream(new File(arguments.get(1)));
        outputStream = new TransactionalFileOutputStream(new File(arguments.get(2)));
        step = 0;
        fileLine = 0;
        lineCount = 0;
    }



    /**
     * Implementation of <code>processing()</code> from
     * <code>MigratableProcess</code>.
     * This function should loop with the <code>suspending</code>
     * and <code>dead</code> flag.
     * First count the number of lines in the input file. Then
     * open the same file again and seek to the output point.
     * Finally, output the tail of the file.
     * The process can resume to a particular step after migration.
     *
     * @throws IOException if any IO error occurs.
     */
    @Override
    public void processing() throws IOException {
    	DataInputStream   reader = new DataInputStream(inputStream);
        PrintStream  writer = new PrintStream(outputStream);
        String line = null;
       

         while(!suspending){
        	switch(step) {
        	case 0:
        		if((line = reader.readLine()) != null)
        			fileLine++;
        		else {
        			reader.close();
        			inputStream.close();
        			inputStream = new TransactionalFileInputStream(new File(arguments.get(1)));
        			reader = new DataInputStream(inputStream);
        			step++;
        		}
        		break;
        	case 1:
        		if(lineCount < fileLine - outputLine) {
	        		reader.readLine();
	        		lineCount++;
        		}
        		else {
        			step++;
        		}
        		break;
        	case 2:
        		if((line = reader.readLine()) != null) {
        			writer.println(line);
        			writer.flush();
        		}
        		else {
        			step++;
        		}
        		break;
        	default:
        		LOG.error("TailProcess[" + id + "]: failed");
        		step = 3;
        	}
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("GrepProcess[" + id + "]: interrupted", e);
            }
        	
            if(step == 3)break;
        }
    }
}
