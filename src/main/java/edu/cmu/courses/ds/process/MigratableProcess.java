package edu.cmu.courses.ds.process;

import edu.cmu.courses.ds.io.TransactionalFileInputStream;
import edu.cmu.courses.ds.io.TransactionalFileOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MigratableProcess implements Runnable, Serializable{
    protected static Logger LOG = LogManager.getLogger(MigratableProcess.class);

    protected TransactionalFileInputStream inputStream;
    protected TransactionalFileOutputStream outputStream;
    protected List<String> arguments;
    protected boolean suspending;
    protected long id;

    public MigratableProcess(){
        initProcess(new String[0]);
    }

    public MigratableProcess(String[] arguments){
        initProcess(arguments);
    }

    public void initProcess(String[] arguments){
        this.arguments = new ArrayList<String>(Arrays.asList(arguments));
        this.suspending = false;
        this.id = ProcessManager.getInstance().generateID();
    }

    public void run(){
        try{
            processing();
        }catch (IOException e){
            LOG.error(this.getClass().getSimpleName() + "[" + id + "]", e);
        }finally {
            ProcessManager.getInstance().finishProcess(this);
            suspending = false;
        }
    }

    public void suspend() throws InterruptedException {
        suspending = true;
        while(suspending){
            Thread.sleep(10);
        }
    }

    public void migrated(){
        this.id = ProcessManager.getInstance().generateID();
        inputStream.setMigrated(true);
        outputStream.setMigrated(true);
    }

    public long getId(){
        return id;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        sb.append("[" + id + "]: ");
        for(int i = 0; i < arguments.size(); i++){
            if(i > 0){
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }
        return sb.toString();
    }

    public abstract void processing() throws IOException;
}
