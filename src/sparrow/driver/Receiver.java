/*
 * Receiver.java
 *
 * @author Juan J. Durillo
 */

package sparrow.driver;

import sparrow.util.SharedObjects;
import sparrow.util.WorkerStatistics;
import sparrow.util.Message;
import sparrow.util.CompletedTask;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import sparrow.worker.WorkerId;

/**
 * This class implementes the receiver thread of the driver
 */

public class Receiver extends Thread {
  
  /**
   * stores the port in wich the thread recived communications
   */
  int port_;
  
  /**
   * stores a server socket in which the driver are listening
   */
  private ServerSocket serverSocket_;
  /**
   * stores shared objects with other threads
   */
  SharedObjects sharedObjects_ ;
  /**
   * Constructor
   */
  public Receiver(SharedObjects sharedObjects, int port) {
    super();
    sharedObjects_ = sharedObjects ;
    port_ = port;
    try {
      serverSocket_ = new ServerSocket(port_);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
  } // Constructor
  
  /**
   * code of the thread behaviour
   */
  public void run() {
    System.out.println("jMetalDriverReceiver. Waiting connection in port " + port_ ) ;
    CompletedTask task;
    boolean finish ;
    while(!sharedObjects_.terminateReceiver_) {
      try {
        task = null;
        // Waiting connection from driver
        Socket socket = serverSocket_.accept() ;
      
        // Obtain the message            
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()) ;
        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
        Object object = inStream.readObject() ;      
        Message message_ = (Message)object ;
        
        // Close the socket
        outStream.close();
        inStream.close();
        socket.close();
        
        // This message is a task
        task = (CompletedTask)message_.getContent();
        if (message_.getSessionId() != sharedObjects_.sessionId_) {
          // Do nothing
          ;
        } else {
          System.out.println("RECEIVER: got task " + task.getId()) ;
          // Add in complete, remove from scheduled
          synchronized(sharedObjects_) {
            sharedObjects_.completedTask_.add(task);
            sharedObjects_.scheduledTask_.remove(task.getId());

          // Get and remove the worker from busy and add to idle
            WorkerId worker = sharedObjects_.busyWorkers_.get(task.getId());
            sharedObjects_.busyWorkers_.remove(task.getId());
            sharedObjects_.idleWorkers_.add(worker);
            
            // Update the time of the task
            WorkerStatistics statistics = sharedObjects_.differentWorkers_.get(worker);
            if (statistics!=null) {
              statistics.updateWorkerStatistics(task.getTime()) ;
              //statistics.updateStatistics(WorkerStatistics.Key.UPDATE_TIME, task.getTime());
            }
          }
        }
      } catch (Exception e) {
        System.err.println("Receiver: error when receiving message") ;
        e.printStackTrace();
      }
    }     
  }// run
} // Receiver
