/**
 * JMetalDriverSender.java
 *
 * @author Antonio J. Nebro
 * @authr Juan J. Durillo
 * @version 1.0
 */
package sparrow.driver;

import sparrow.util.SharedObjects;
import sparrow.util.Message;
import sparrow.util.Task;
import java.io.*;
import java.net.Socket;
import java.util.*;
import sparrow.applications.Monitor;

import sparrow.worker.WorkerId;

/**
 * This class implementes the sender thread of the driver
 */
public class Sender extends Thread {

  /**
   * store all the diferents workers that have been 
   * participated in the computation
   */
  
  SharedObjects sharedObjects_ ;
  /**
   * Constructor
   */
  public Sender(SharedObjects sharedObjects) {
    super();
    sharedObjects_ = sharedObjects ;
  } // Constructor
  
  
  /**
   * Sends a task to a worker
   * @param task the task
   * @param worker the worker
   */
  public void send(Task task, WorkerId worker) throws Exception {
    Socket socket;
    ObjectOutputStream outStream;
    ObjectInputStream inStream;
    Message taskMessage, response;
    
    socket = new Socket(worker.getIP(),worker.getPort());
    outStream = new ObjectOutputStream(socket.getOutputStream()) ;
    inStream   = new ObjectInputStream(socket.getInputStream()) ;
    
    System.out.println("SENDER: sending task " + task.getId()) ;
    
    taskMessage = new Message(sharedObjects_.sessionId_,
                              Message.EXECUTE,
                              task);
    outStream.writeObject(taskMessage) ;
    
    response = (Message)inStream.readObject();     
    
    outStream.close();
    inStream.close();
    socket.close();
    if (response.getType() != Message.EXECUTE_ACK) {
      throw new SenderException();
    }
  } // send
  
  
  /**
   * Run method
   * @throws  
   */
  public void run() {
    // Endless loop
    boolean finish ;

    finish = false ;
    while (!sharedObjects_.terminate_) {
      WorkerId worker   = null;
      Task task = null;
      try {
    	  worker = sharedObjects_.idleWorkers_.get();
    	  task   = sharedObjects_.nonScheduledTask_.get();
    	  if (worker == null) {
    		  finish = true;
    	  } else //// CAMBIOOOOOOOOOOOOOOOOOOOOOOOOOO
    		  if (task == null) { // terminate
    			  finish = true ;
    		  } else {
    			  synchronized(sharedObjects_) {
    				  // Enviar el mensaje a un worker.
    				  send(task,worker);  
    				  // Removes task and worker from the list
    				  sharedObjects_.idleWorkers_.remove();
    				  sharedObjects_.nonScheduledTask_.remove();
    				  sharedObjects_.busyWorkers_.add(task.getId(),worker);
    				  sharedObjects_.scheduledTask_.add(task.getId(),task);
    			  }
    		  }
      } catch (Exception e) {
    	  // The task can have been scheduled into the worker.
    	  // It is possible that the worker are corrupt, 
    	  //then remove from the list and add at the end
    	  if (worker != null) {
    	  	synchronized(sharedObjects_) {
    		    sharedObjects_.idleWorkers_.remove();
                    sharedObjects_.allWorkers_.remove(worker);
                    List<WorkerId> toReset = new ArrayList<WorkerId>();
                    toReset.add(worker);
                    Monitor.reset(toReset);
    		   // sharedObjects_.idleWorkers_.add(worker);
    	  	} 
    	  }
      }
    } // while
  } // run
} // Sender
