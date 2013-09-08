package edu.cmu.courses.ds.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class ProcessManager {
    private static Logger LOG = LogManager.getLogger(ProcessManager.class);
    private static ProcessManager singleton;

    private AtomicLong idCounter;
    private ConcurrentLinkedQueue<MigratableProcess> processes;
    Set<Class<? extends MigratableProcess>> processClasses;

    private ProcessManager(){
        idCounter = new AtomicLong(0);
        processes = new ConcurrentLinkedQueue<MigratableProcess>();
        Reflections reflections = new Reflections("edu.cmu.courses.ds.process");
        processClasses = reflections.getSubTypesOf(MigratableProcess.class);
    }

    public void startServer(){
        Thread serverThread = new Thread(new ProcessServer());
        serverThread.start();
    }

    public void startConsole(){
        System.out.println("Welcome aboard! Type 'help' for more information");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.print("> ");
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                LOG.fatal("read command error", e);
                System.exit(-1);
            }
            processCommand(line);
        }
    }

    public long generateID(){
        return idCounter.getAndIncrement();
    }

    public void finishProcess(MigratableProcess process){
        processes.remove(process);
    }

    public boolean startProcess(String processName, String[] args)
            throws IllegalAccessException, InstantiationException {
        Iterator<Class<? extends MigratableProcess>> it = processClasses.iterator();
        while(it.hasNext()){
            Class<? extends MigratableProcess> process = it.next();
            if(process.getSimpleName().equals(processName)){
                MigratableProcess processInstance = process.newInstance();
                processInstance.initProcess(args);
                startProcess(processInstance);
                return true;
            }
        }
        return false;
    }

    public void startProcess(MigratableProcess process){
        Thread thread = new Thread(process);
        thread.start();
        processes.offer(process);
    }

    synchronized public static ProcessManager getInstance(){
        if(singleton == null){
            singleton = new ProcessManager();
        }
        return singleton;
    }

    private MigratableProcess getProcess(long id){
        Iterator<MigratableProcess> it = processes.iterator();
        while(it.hasNext()){
            MigratableProcess process  = it.next();
            if(process.getId() == id)
                return process;
        }
        return null;
    }

    private void processCommand(String commandLine){
        if(commandLine == null || commandLine.length() == 0)
            return;
        String[] args = commandLine.split("\\s+");
        if(args.length == 0)
            return;
        switch (ProcessManagerCommand.getInstance(args[0].toLowerCase())){
            case HELP:
                processHelpCommand();
                break;
            case QUIT:
                processQuitCommand();
                break;
            case LS:
                processLsCommand();
                break;
            case PS:
                processPsCommand();
                break;
            case RUN:
                processRunCommand(args);
                break;
            case MG:
                processMigrateCommand(args);
                break;
            case UNKNOWN:
            default:
                System.out.println("unknown command '" + args[0] + "'");
                break;
        }
    }

    private void processLsCommand(){
        if(processClasses.isEmpty()){
            System.out.println("No migratable program ");
        } else {
            System.out.println("All migratable programs:");
            System.out.println("-------------------------------");
            Iterator<Class<? extends MigratableProcess>> it = processClasses.iterator();
            while(it.hasNext()){
                Class<? extends MigratableProcess> processClass = it.next();
                System.out.println(processClass.getSimpleName());
            }
        }
    }

    private void processPsCommand(){
        if(processes.isEmpty()){
            System.out.println("No running process");
        } else {
            Iterator<MigratableProcess> it = processes.iterator();
            while(it.hasNext()){
                MigratableProcess process = it.next();
                System.out.println(process.toString());
            }
        }
    }

    private void processRunCommand(String[] args){
        if(args.length <= 1){
            System.out.println("usage: run PROCESS_NAME ARG...");
        } else {
            String processName = args[1];
            String[] processArgs = new String[args.length - 2];
            for(int i = 2; i < args.length; i++){
                processArgs[i - 2] = args[i];
            }
            boolean contains = false;
            try {
                contains = startProcess(processName, processArgs);
            } catch (Exception e) {
                LOG.error("run command " + processName + " error", e);
                return;
            }
            if(!contains){
                System.out.println("No such program: '" + processName + "'");
            }
        }
    }

    private void processQuitCommand(){
        System.out.println("Bye!");
        System.exit(0);
    }

    private void processMigrateCommand(String[] args){
        if(args.length <= 2){
            System.out.println("usage: mg PROCESS_ID HOSTNAME");
        } else {
            long id = Long.parseLong(args[1]);
            String hostName = args[2];
            MigratableProcess process = getProcess(id);
            if(process == null){
                System.out.println("No such process: " + args[1]);
                return;
            }
            try {
                process.suspend();
            } catch (InterruptedException e) {
                LOG.error(process.getClass().getSimpleName() +
                          "[" + id + "] suspend error", e);
                return;
            }
            startMigrating(process, hostName);
        }

    }

    private void startMigrating(MigratableProcess process, String hostName){
        Socket socket = null;
        ObjectOutputStream out = null;
        DataInputStream in = null;
        boolean status = false;
        try {
            socket = new Socket(hostName, ProcessServer.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            out.writeObject(process);
            status = in.readBoolean();
            if(status)
                System.out.println("Successfully migrated " +
                                   process.getClass().getSimpleName() +
                                   "[" + process.getId() + "]");
            else
                System.out.println("Failed to migrate " +
                                   process.getClass().getSimpleName() +
                                   "[" + process.getId() + "]");
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Connect " + hostName + " failed: " +
                               e.getMessage());
            return;
        }
    }

    private void processHelpCommand(){
        StringBuffer sb = new StringBuffer();
        sb.append("All commands are listed as below\n");
        sb.append("ls:   list all migratable programs\n");
        sb.append("ps:   list all running process\n");
        sb.append("run:  start process.\n");
        sb.append("      run PROCESS_NAME ARG...\n");
        sb.append("mg:   migrate process to another machine\n");
        sb.append("      mg PROCESS_ID HOSTNAME\n");
        sb.append("quit: quit Process Manager\n");
        sb.append("help: show help information\n");
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        ProcessManager.getInstance().startServer();
        ProcessManager.getInstance().startConsole();
    }
}
