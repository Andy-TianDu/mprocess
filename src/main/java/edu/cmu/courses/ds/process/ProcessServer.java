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

public class ProcessServer implements Runnable{
    public static final int PORT = 15440;
    private static Logger LOG = LogManager.getLogger(ProcessServer.class);

    private ServerSocket serverSocket;
    private boolean running;

    @Override
    public void run() {
        running = true;
        bind();
        while(running){
            accept();
        }
    }

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

    private void bind(){
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            LOG.fatal("ServerSocket bind error", e);
            System.exit(-1);
        }
    }

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
