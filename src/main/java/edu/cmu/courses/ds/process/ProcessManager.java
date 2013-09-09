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

/**
 * The manager of all migratable processes
 * This manager contains two main parts: the first is the
 * <code>ProcessServer</code>, which is designed to receive the
 * migration request from other <code>ProcessManager</code>; The
 * second part is a console, which is designed to offer a controller
 * interface. Other functions in this class contains generator of
 * process ID, callback function of process exit, etc,.
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.process.MigratableProcess
 * @see edu.cmu.courses.ds.process.ProcessServer
 */
public class ProcessManager {
    /**
     * Log handler
     *
     * @see <a href="http://logging.apache.org/log4j/2.x/">Log4J</a>
     */
    private static Logger LOG = LogManager.getLogger(ProcessManager.class);

    /**
     * The singleton instance of <code>ProcessManager</code>
     */
    private static ProcessManager singleton;

    /**
     * The counter for process ID. By using the <code>AtomicLong</code>,
     * We assure the ID generation is thread-safe.
     *
     * @see java.util.concurrent.atomic.AtomicLong
     */
    private AtomicLong idCounter;

    /**
     * The linked queue of current processes.
     * By using the <code>ConcurrentLinkedQueue</code> we assure the
     * queue operations(add, remove...) are thread-safe.
     *
     * @see java.util.concurrent.ConcurrentLinkedQueue
     */
    private ConcurrentLinkedQueue<MigratableProcess> processes;

    /**
     * The set of all migratable classes inherited from
     * <code>MigratableProcess</code>
     *
     * @see edu.cmu.courses.ds.process.MigratableProcess
     */
    Set<Class<? extends MigratableProcess>> processClasses;

    /**
     * Constructor of <code>ProcessManager</code>
     * The constructor is invisible since we need to keep
     * the <code>ProcessManager</code> is single instance.
     * In the constructor we use <code>Reflections</code> library
     * to get all classes inherited from <code>MigratableProcess</code>
     *
     * @see <a href="https://code.google.com/p/reflections/">Reflections Library</a>
     */
    private ProcessManager() {
        idCounter = new AtomicLong(0);
        processes = new ConcurrentLinkedQueue<MigratableProcess>();
        Reflections reflections = new Reflections("edu.cmu.courses.ds.process");
        processClasses = reflections.getSubTypesOf(MigratableProcess.class);
    }

    /**
     * Start the <code>ProcessServer</code> in a new <code>Thread</code>
     *
     * @see edu.cmu.courses.ds.process.ProcessServer
     * @see java.lang.Thread#start()
     */
    public void startServer() {
        Thread serverThread = new Thread(new ProcessServer());
        serverThread.start();
    }

    /**
     * Start the interactive console of <code>ProcessManager</code>.
     * Read user command from <code>System.in</code>, then process
     * the command.
     *
     * @see edu.cmu.courses.ds.process.ProcessManager#processCommand(String)
     */
    public void startConsole() {
        System.out.println("Welcome aboard! Type 'help' for more information");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
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

    /**
     * Generate a process ID by using <code>getAndIncrement</code>.
     * This function is thread-safe
     *
     * @return the process ID
     * @see java.util.concurrent.atomic.AtomicLong#getAndIncrement()
     */
    public long generateID() {
        return idCounter.getAndIncrement();
    }

    /**
     * Callback for process exit, remove process from <code>processes</code>
     * queue. The <code>processes.remove(Object)</code> is thread-safe.
     *
     * @param process the process instance
     * @see java.util.concurrent.ConcurrentLinkedQueue#remove(Object)
     */
    public void finishProcess(MigratableProcess process) {
        processes.remove(process);
    }

    /**
     * Start a process by using <code>processName</code> and
     * <code>args</code>. We lookup the <code>processName</code>
     * in <code>processClasses</code>, then get the
     * <code>Class</code> object and call <code>initProcess</code>
     * to initialize the <code>MigratableProcess</code>. Next,
     * we Java's reflection to create a new process instance.
     * Finally we add the process object to our queue.
     *
     * @param processName the process name
     * @param args        the process arguments
     * @return if success return <code>true</code>
     *         else return <code>false</code>
     * @throws IllegalAccessException can't access process constructor
     * @throws InstantiationException can't find default process constructor
     * @see Class#newInstance()
     * @see edu.cmu.courses.ds.process.MigratableProcess#initProcess(String[])
     * @see edu.cmu.courses.ds.process.ProcessManager#startProcess(MigratableProcess)
     */
    public boolean startProcess(String processName, String[] args)
            throws IllegalAccessException, InstantiationException {
        Iterator<Class<? extends MigratableProcess>> it = processClasses.iterator();
        while (it.hasNext()) {
            Class<? extends MigratableProcess> process = it.next();
            if (process.getSimpleName().equals(processName)) {
                MigratableProcess processInstance = process.newInstance();
                processInstance.initProcess(args);
                startProcess(processInstance);
                return true;
            }
        }
        return false;
    }

    /**
     * Start a process by using <code>MigratableProcess</code> object.
     * Add the process object to the linked queue. This function is
     * thread-safe.
     *
     * @param process
     * @see java.util.concurrent.ConcurrentLinkedQueue#offer(Object)
     */
    public void startProcess(MigratableProcess process) {
        
        try{
        	process.resume();
        }
    	catch (InterruptedException e) {
            LOG.error(getClass().getSimpleName() +
                    "resume error", e);
            return;
    	}
        Thread thread = new Thread(process);
        thread.start();
        processes.offer(process);
    }

    /**
     * Get the singleton <code>ProcessManager</code> instance.
     * This function is thread-safe.
     *
     * @return the <code>ProcessManager</code> instance
     */
    synchronized public static ProcessManager getInstance() {
        if (singleton == null) {
            singleton = new ProcessManager();
        }
        return singleton;
    }

    /**
     * Lookup a process in <code>processes</code> by process ID
     *
     * @param id process ID
     * @return the <code>MigratableProcess</code> object if found,
     *         else return null
     */
    private MigratableProcess getProcess(long id) {
        Iterator<MigratableProcess> it = processes.iterator();
        while (it.hasNext()) {
            MigratableProcess process = it.next();
            if (process.getId() == id)
                return process;
        }
        return null;
    }

    /**
     * Process command line.
     * Split the command line with blank. The first block is
     * the command, others are command arguments. We use
     * <code>ProcessManagerCommand</code> enum to switch between
     * commands.
     *
     * @param commandLine the user input command line
     * @see java.lang.String#split(String)
     * @see edu.cmu.courses.ds.process.ProcessManagerCommand
     */
    private void processCommand(String commandLine) {
        if (commandLine == null || commandLine.length() == 0)
            return;
        String[] args = commandLine.split("\\s+");
        if (args.length == 0)
            return;
        switch (ProcessManagerCommand.getInstance(args[0].toLowerCase())) {
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

    /**
     * List all classes inherited from <code>MigratableProcess</code>
     */
    private void processLsCommand() {
        if (processClasses.isEmpty()) {
            System.out.println("No migratable program ");
        } else {
            System.out.println("All migratable programs:");
            System.out.println("-------------------------------");
            Iterator<Class<? extends MigratableProcess>> it = processClasses.iterator();
            while (it.hasNext()) {
                Class<? extends MigratableProcess> processClass = it.next();
                System.out.println(processClass.getSimpleName());
            }
        }
    }

    /**
     * List all running processes
     */
    private void processPsCommand() {
        if (processes.isEmpty()) {
            System.out.println("No running process");
        } else {
            Iterator<MigratableProcess> it = processes.iterator();
            while (it.hasNext()) {
                MigratableProcess process = it.next();
                System.out.println(process.toString());
            }
        }
    }

    /**
     * Run a process by using process name and arguments.
     *
     * @param args command arguments
     * @see edu.cmu.courses.ds.process.ProcessManager#startProcess(String, String[])
     */
    private void processRunCommand(String[] args) {
        if (args.length <= 1) {
            System.out.println("usage: run PROCESS_NAME ARG...");
        } else {
            String processName = args[1];
            String[] processArgs = new String[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                processArgs[i - 2] = args[i];
            }
            boolean contains = false;
            try {
                contains = startProcess(processName, processArgs);
            } catch (Exception e) {
                LOG.error("run command " + processName + " error", e);
                return;
            }
            if (!contains) {
                System.out.println("No such program: '" + processName + "'");
            }
        }
    }

    /**
     * Quit the program
     */
    private void processQuitCommand() {
        System.out.println("Bye!");
        System.exit(0);
    }

    /**
     * Migrate the specific process by using process ID
     * First lookup the process by ID, then suspend the process.
     * Finally we <code>statMigrating()</code>
     *
     * @param args command arguments
     * @see edu.cmu.courses.ds.process.ProcessManager#getProcess(long)
     * @see edu.cmu.courses.ds.process.ProcessManager#startMigrating(MigratableProcess, String)
     */
    private void processMigrateCommand(String[] args) {
        if (args.length <= 2) {
            System.out.println("usage: mg PROCESS_ID HOSTNAME");
        } else {
            long id = Long.parseLong(args[1]);
            String hostName = args[2];
            MigratableProcess process = getProcess(id);
            if (process == null) {
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

    /**
     * Start migrating the process to specific host.
     * First we connect the specific host by <code>Socket</code>.
     * Then we send the entire <code>MigratableProcess</code> object
     * by using <code>ObjectOutputStream</code>. Finally we receive
     * ths migration status from host by using <code>DataInputStream</code>
     *
     * @param process the process object
     * @param hostName the host name which the object will migrate to
     * @see java.net.Socket
     * @see java.io.DataInputStream
     * @see java.io.ObjectOutputStream
     */
    private void startMigrating(MigratableProcess process, String hostName) {
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
            if (status) {
            	try {
            		process.kill();
            	}
            	catch (InterruptedException e) {
	                LOG.error(process.getClass().getSimpleName() +
	                        "[" + process.getId() + "] kill error", e);
	                return;
            	}
                System.out.println("Successfully migrated " +
                        process.getClass().getSimpleName() +
                        "[" + process.getId() + "]");
            }
            else
            {
            	try {
            		process.resume();
            	}
            	catch (InterruptedException e) {
	                LOG.error(process.getClass().getSimpleName() +
	                        "[" + process.getId() + "] resume error", e);
	                return;
            	}
                System.out.println("Failed to migrate " +
                        process.getClass().getSimpleName() +
                        "[" + process.getId() + "]");
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
        	try {
        		process.resume();
        	}
        	catch (InterruptedException e1) {
                LOG.error(process.getClass().getSimpleName() +
                        "[" + process.getId() + "] resume error", e1);
        	}
            System.out.println("Connect " + hostName + " failed: " +
                    e.getMessage());
            return;
        }
    }

    /**
     * Print the help information
     */
    private void processHelpCommand() {
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

    /**
     * Main function.
     * Start <code>ProcessManager</code> server and console
     *
     * @param args program augments
     * @see edu.cmu.courses.ds.process.ProcessManager#startServer()
     * @see edu.cmu.courses.ds.process.ProcessManager#startConsole()
     */
    public static void main(String[] args) {
        ProcessManager.getInstance().startServer();
        ProcessManager.getInstance().startConsole();
    }
}
