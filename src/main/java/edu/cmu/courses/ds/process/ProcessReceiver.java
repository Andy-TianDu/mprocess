package edu.cmu.courses.ds.process;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * The socket receiver of socket server.
 * Each receiver is a single thread that communicate with
 * one client. It implements the <code>run()</code> from
 * the <code>Runnable</code>interface.
 *
 * @author Jian Fang(jianf)
 * @author Fangyu Gao(fangyug)
 * @see edu.cmu.courses.ds.process.ProcessServer
 */
public class ProcessReceiver implements Runnable{
	/**
	 * Socket communication with client.
	 */
	Socket clientSocket;
	
	/**
	 * Constructor with an already exist socket as input.
	 */
	ProcessReceiver(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
    /**
     * The implementation of <code>Runnable</code> interface.
     * After connected and received process, the receiver
     * determine which class the process is, then send a
     * signal to the client to tell if the migration succeed.
     */
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			
			Object object = in.readObject();
			
			MigratableProcess process = null;
            if(object instanceof MigratableProcess){
            	process = (MigratableProcess)object;
            	process.migrated();
            	out.writeBoolean(true);
	            ProcessManager.getInstance().startProcess(process);
            }
            else {
            	out.writeBoolean(false);
            }
            in.close();
            out.close();
            clientSocket.close();
		}
		catch (IOException e) {
			System.out.println("processing client request error");
        } catch (ClassNotFoundException e) {
        	System.out.println("client sent unrecognized object");
        }
	}
}
