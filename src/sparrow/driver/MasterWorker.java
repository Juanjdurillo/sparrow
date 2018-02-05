/**
 * MasterWorker.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 * @version 1.0
 */
package sparrow.driver;

import sparrow.util.InitData;
import sparrow.util.SharedObjects;
import sparrow.util.CompletedTask;
import java.io.IOException;
import java.util.Date;

import sparrow.applications.Monitor;


public abstract class MasterWorker {  
  /**
   * stores the shared object
   */
  protected SharedObjects sharedObjects_ ;
  
  /**
   * stores the port in which the WorkersServer is waiting to assign workers
   */
  private int workersServerPort_;
  
  /**
   * stores the IP in which the WorkersServer is waiting to assign workers
   */
  private String workersServerIP_;
  
  /**
   * stores the control_ thread
   */
  public Control control_;
  
  /**
   * stores the sender_ thread
   */
  public Sender sender_;
  
  /**
   * stores the receiver_ thread
   */
  public Receiver receiver_;


  // Abstract methods to redefine
  public abstract InitData setInitData() ;  
  public abstract void CreateInitialTasks() ;
  public abstract void newCompletedTask(CompletedTask completedTask) ;
  public abstract void printResults();
  
  /**
   * Constructor
   */
  public MasterWorker(int port, String IP) {
    
    // get the port
    workersServerPort_ = port;
    // get the IP
    workersServerIP_   = IP;
    
    // instantiate the shared object variable
    sharedObjects_ = new SharedObjects();
    
    // Create a random port between 10,000 and 30,000 in which the receiver listenes
    int receiverPort = 10000 + (new java.util.Random()).nextInt(20000); 
    
    // Create the control_, sender_ and receiver_ objects
    control_  = new Control(sharedObjects_,receiverPort, workersServerPort_,workersServerIP_);
    sender_   = new Sender(sharedObjects_);
    receiver_ = new Receiver(sharedObjects_,receiverPort);
  } // MasterWorker
  
  /**
   * 
   */
  public void execute()  {
    sharedObjects_.startTime_ = new Date() ;
    
    // Step 1: Create initial data
    sharedObjects_.initData_ = setInitData() ;
    
    if (sharedObjects_.initData_== null) {
      System.err.println("The initial task is undefined");
      System.exit(0);
    }
    
    // Step 3: Launch the control, sender and receiver threads
    Thread threadControl, threadSender, threadReceiver;
    threadControl = new Thread(control_);
    threadSender = new Thread(sender_);
    threadReceiver = new Thread(receiver_);
    threadControl.start();
    threadSender.start();
    threadReceiver.start();
    
    // Step 4: Create the initial tasks
    CreateInitialTasks() ;
    
    //(new Thread(sender_)).start();
    //(new Thread(receiver_)).start();

    
    // Step 5: waiting task and calling the new CompletedTask method
    CompletedTask completedTask;
    while (!sharedObjects_.terminate_) {
      try {
        completedTask = sharedObjects_.completedTask_.get();
        sharedObjects_.completedTask_.remove();
        if (completedTask != null) {
          newCompletedTask(completedTask);
        }
      } catch (Exception e) {
        System.err.println("Exception in "+ e);
        e.printStackTrace();
        System.exit(0);
      }
      
      if (sharedObjects_.terminationDetection()) {
        sharedObjects_.terminate_= true;
      } // if
    }
    sharedObjects_.endTime_ = new Date() ;

    control_.endSession();
    
    sharedObjects_.terminateReceiver_ = true ;
    // Step 6: Invoke printResults
    printResults() ;
    
    // Step 7: Invoke print statistics over the control
    control_.statistics(sharedObjects_.differentWorkers_.values());
    // Stop the treads
    System.exit(0);
  }
  
  //public static void main(String[] args) throws IOException,
 // ClassNotFoundException {
  ///  Algorithm algorithm = new Algorithm() ;  
  //} // main
} // MasterWorker
