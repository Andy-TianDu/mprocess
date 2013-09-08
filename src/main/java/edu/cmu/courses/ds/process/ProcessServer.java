package edu.cmu.courses.ds.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * The socket server of process manager.
 * This sever is designed to handle the migration request
 * from other <code>ProcessManager</code>. It implements
 * the <code>run()</code> from the <code>Runnable</code>
 * interface.
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see java.net.ServerSocket
 * @see java.net.Socket
 * @see edu.cmu.courses.ds.process.MigratableProcess
 */
public class ProcessServer implements Runnable{
    /**
     * Default port number of process server
     */
    public static final int PORT = 15440;

    /**
     * Log handler
     *
     * @see <a href="http://logging.apache.org/log4j/2.x/">Log4J</a>
     */
    private static Logger LOG = LogManager.getLogger(ProcessServer.class);

    /**
     * Server socket of process server
     */
    private ServerSocket serverSocket;

    /**
     * Running flag
     */
    private boolean running;

    /**
     * The implementation of <code>Runnable</code> interface
     * First <code>bind()</code> the port, then idle loop to
     * <code>accept()</code> migration request. If the running
     * flag is not set, the function breaks the loop.
     */
    @Override
    public void run() {
        running = true;
        bind();
        while(running){
            accept();
        }
    }

    /**
     * Stop the process server.
     * Unset the running flag, then call <code>serverSocket.close()</code>
     * This close operation will cause an <code>SocketException</code> in
     * <code>accept()</code>.
     *
     * @see java.net.ServerSocket#close()
     */
    public void stop(){
        if(running){
            running = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOG.error("stop process server error", e);
                System.exit(-1);
            }
        }
    }

    /**
     * Bind the <code>PORT</code>.
     * If bind failed, the program exit with -1.
     */
    private void bind(){
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            LOG.fatal("ServerSocket bind error", e);
            System.exit(-1);
        }
    }

    /**
     * Accept migration request from other <code>ProcessManager</code>.
     * If the function caught a <code>SocketException</code> and the
     * <code>running</code> flag is unset, we assert <code>stop()</code>
     * is called, so exit the program normally. If not, something goes
     * wrong, we exit the program with status -1.
     *
     * @see java.net.ServerSocket#accept()
     * @see edu.cmu.courses.ds.process.ProcessServer#stop()
     */
    private void accept(){
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        }catch (SocketException e){
            if(running){
                LOG.fatal("server socket error", e);
                System.exit(-1);
            }
        } catch (IOException e) {
            LOG.fatal("ServerSocket accept error", e);
            System.exit(-1);
        }
        processRequest(clientSocket);
    }

    /**
     * Process the migration request.
     * First, read the <code>MigratableProcess</code> object from socket
     * by using <code>ObjectInputStream</code>. Then resume the process.
     *
     * @param clientSocket the client socket
     * @see java.io.ObjectInputStream#readObject()
     * @see edu.cmu.courses.ds.process.ProcessServer#processRequest(java.net.Socket)
     */
    private void processRequest(Socket clientSocket){
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            Object object = in.readObject();
            if(object instanceof MigratableProcess){
                processMigration(clientSocket.getOutputStream(),
                                (MigratableProcess)object);
            }
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            LOG.error("processing client request error", e);
        } catch (ClassNotFoundException e) {
            LOG.warn("client sent unrecognized object");
        }
    }

    /**
     * Resume the migrated process.
     * Start a new thread to run the migrated process.
     *
     * @param outputStream socket outputStream
     * @param process migrated process
     * @throws IOException if some IO errors occur
     * @see java.io.PrintWriter#write(String)
     * @see edu.cmu.courses.ds.process.ProcessManager#startProcess(MigratableProcess)
     */
    private void processMigration(OutputStream outputStream,
                                  MigratableProcess process)
            throws IOException {
        PrintWriter out = new PrintWriter(outputStream);
        process.migrated();
        ProcessManager.getInstance().startProcess(process);
        out.print(true);
        out.close();
    }
}