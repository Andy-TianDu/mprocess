package edu.cmu.courses.ds.process;


import edu.cmu.courses.ds.io.TransactionalFileInputStream;
import edu.cmu.courses.ds.io.TransactionalFileOutputStream;

import java.io.*;

public class GrepProcess extends MigratableProcess {

    public GrepProcess(){
        super();
    }

    public GrepProcess(String[] args){
        super(args);
    }

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

        while(!suspending){
            String line = reader.readLine();
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

        reader.close();
        writer.close();

        inputStream.close();
        outputStream.close();
    }
}
