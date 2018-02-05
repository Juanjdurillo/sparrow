/**
 * Worker.java
 * @author Juan J. Durillo
 */
package sparrow.worker;

import sparrow.util.InitData;
import sparrow.util.Message;
import sparrow.util.CompletedTask;
import sparrow.util.Task;
import sparrow.util.SystemClassLoaderManager;
import java.io.* ;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

//import nsgaIIdist.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;


public class Worker implements Runnable{
  /**
   * Machine information
   */
  //static String systemInfo_ [] ; 
  
  /**
   * System class loader manager 
   */
  SystemClassLoaderManager systemClassLoaderManager_ ;
  
  /**
   * Logger and file handler
   */
  private static Logger      logger_ ;
  private static FileHandler fileHandler_ ;
  
  /**
   * FREE, IDLE and BUSY define all the valid status
   */
  public static final int FREE = 0,
                          IDLE = 1,
                          BUSY = 2,
                          ENDING_SESSION = 3;
  
  
  /**
   * stores the current worker status
   */
  private int status_;
 
  /**
   * stores a socket for communications
   */
  ServerSocket serverSocket_;
  
  /**
   * listening port for the socket
   */
  private int port_ ;
  
  /**
   * status table
   */
  private Method [][] statusTable_ ;
  
  /**
   * stores the last received message 
   */
  private Message message_;
  
  /**
   * stores the task to execute
   */
  Task task_ ;
  
  /**
   * stores the parameter for the task
   */
  private InitData initData_;
  
  /**
   * stores a thread for executing task
   */
  private Thread executionThread_;
  
  /**
   * stores the session id
   */
  private int sessionId_ = -1;
  
  /**
   * stores the end condition
   */
  private boolean fin_;
  
  /**
   * stores the reset condition
   */
  private boolean reset_;
  
  
  /**
   * Creates a new Worker
   */
  public Worker(int port) {
//    OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean() ;
//    systemInfo_ = new String[5] ;
//    systemInfo_[0] =  bean.getArch() ;
//    systemInfo_[1] =  bean.getName() ; 
//    systemInfo_[2] =  bean.getVersion() ;
//    systemInfo_[3] =  "" + bean.getAvailableProcessors() ;
//    systemInfo_[4] =  "" + bean.getSystemLoadAverage() ;
//    
//    System.out.println("Architecture: " + systemInfo_[0]) ;
//    System.out.println("Op. System  : " + systemInfo_[1]) ;
//    System.out.println("Version     : " + systemInfo_[2]) ;
//    System.out.println("Processors  : " + systemInfo_[3]) ;
//    System.out.println("Load Aver.  : " + systemInfo_[4]) ;
//    
    
    systemClassLoaderManager_ = null ;
    
    //systemInfo_ = new String[3] ; // arch, os, date

    //systemInfo_[0] = System.getProperty("os.arch") ;
    //systemInfo_[1] = System.getProperty("os.name") ;

    port_ = port ;
    status_ = FREE;
    fin_    = false;
    try {
      serverSocket_ = new ServerSocket(port_);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    // DEFINE HERE THE STATUS MACHINE FOR THE WORKER
    statusTable_ = new Method[4][6]; // Three state, three messages
   
    Class [] paramList = new Class[1];
    paramList[0] = Message.class;
    try {
      statusTable_[FREE][Message.EXECUTE]      = Worker.class.getMethod("printError",paramList);
      statusTable_[FREE][Message.STATUS]       = Worker.class.getMethod("printState",paramList);
      statusTable_[FREE][Message.INIT_SESSION] = Worker.class.getMethod("receiveInitData",paramList);
      statusTable_[FREE][Message.END_SESSION]  = Worker.class.getMethod("printError",paramList);
      statusTable_[FREE][Message.RESET]        = Worker.class.getMethod("reset",paramList);
      statusTable_[FREE][Message.TERMINATE]    = Worker.class.getMethod("terminate",paramList);
      
      statusTable_[IDLE][Message.EXECUTE]      = Worker.class.getMethod("executeTask",paramList);
      statusTable_[IDLE][Message.STATUS]       = Worker.class.getMethod("printState",paramList);
      statusTable_[IDLE][Message.INIT_SESSION] = Worker.class.getMethod("printError",paramList);
      statusTable_[IDLE][Message.END_SESSION]  = Worker.class.getMethod("idleEndSession",paramList);
      statusTable_[IDLE][Message.RESET]        = Worker.class.getMethod("reset",paramList);
      statusTable_[IDLE][Message.TERMINATE]    = Worker.class.getMethod("terminate",paramList);
      
      statusTable_[BUSY][Message.EXECUTE]      = Worker.class.getMethod("printError",paramList);
      statusTable_[BUSY][Message.STATUS]       = Worker.class.getMethod("printState",paramList);
      statusTable_[BUSY][Message.INIT_SESSION] = Worker.class.getMethod("printError",paramList);
      statusTable_[BUSY][Message.END_SESSION]  = Worker.class.getMethod("busyEndSession",paramList);
      statusTable_[BUSY][Message.RESET]        = Worker.class.getMethod("reset",paramList);
      statusTable_[BUSY][Message.TERMINATE]    = Worker.class.getMethod("terminate",paramList);
      
      statusTable_[ENDING_SESSION][Message.EXECUTE]      = Worker.class.getMethod("printError",paramList);
      statusTable_[ENDING_SESSION][Message.STATUS]       = Worker.class.getMethod("printState",paramList);
      statusTable_[ENDING_SESSION][Message.INIT_SESSION] = Worker.class.getMethod("printError",paramList);
      statusTable_[ENDING_SESSION][Message.END_SESSION]  = Worker.class.getMethod("printError",paramList);
      statusTable_[ENDING_SESSION][Message.RESET]        = Worker.class.getMethod("reset",paramList);
      statusTable_[ENDING_SESSION][Message.TERMINATE]    = Worker.class.getMethod("terminate",paramList);
      
    } catch (SecurityException ex) {
      ex.printStackTrace();
    } catch (NoSuchMethodException ex) {
      ex.printStackTrace();
    }
  } // Worker
  
  /**
   * this method print a error message on the screen
   */
  public void printError(Message message) {
    logger_.severe("WORKER: printError() invoked") ;
    message.setType(Message.ERROR);
    message.setContent(null);
  } // printError
  
  /**
   * Received terminate message
   */
  public void terminate(Message message) {
    logger_.info("WORKER: receiving a TERMINATE message");    
    changeStatus(ENDING_SESSION); // This avoids that the thread try to return the current task in execution
    message.setType(Message.TERMINATE_ACK) ;
    message.setContent(null) ;
    fin_ = true;
  } // terminate
  
  
  /**
   * Received reset message
   */
  public void reset(Message message) {
    logger_.info("WORKER: receiving a RESET message");    
    changeStatus(ENDING_SESSION); // This avoids that the thread try to return the current task in execution
    message.setType(Message.RESET_ACK) ;
    message.setContent(null) ;
    reset_ = true;
  } // terminate
  
  
  /**
   * This method is used to receive a task
   * @throws InvocationTargetException 
   * @throws IllegalAccessException 
   * @throws NoSuchMethodException 
   * @throws IllegalArgumentException 
   * @throws SecurityException 
   */
  public void receiveInitData(Message message) throws SecurityException, 
                                                      IllegalArgumentException, 
                                                      NoSuchMethodException, 
                                                      IllegalAccessException, 
                                                      InvocationTargetException {
    logger_.info("WORKER: Receiving init data");
    sessionId_ = message.getSessionId();
    
    if (systemClassLoaderManager_ == null) {
      logger_.fine("WORKER: Receiving URL");

      URL[] urls = (URL[])message_.getContent() ;
      systemClassLoaderManager_ = new SystemClassLoaderManager() ;
      if (urls != null) {
        systemClassLoaderManager_.updateSystemClassManager(urls) ;
      } // if
      systemClassLoaderManager_.printURLs() ;
      changeStatus(FREE);
      message.setType(Message.INIT_SESSION_ACK);
    } else {
      logger_.fine("WORKER: Receiving InitData");

      initData_ = (InitData)message_.getContent();

//    System.out.println("Problem: "+ ((InitData)initData_).cadena ) ;
//    System.out.println("Problem: "+ ((InitData)initData_).problem_.getName()) ;
      logger_.fine("WORKER: Received init data") ;
      changeStatus(IDLE);
      message.setType(Message.INIT_SESSION_ACK);
      
      systemClassLoaderManager_.printURLs() ;
    }
  } // receiveInitData
  
  /**
   * This method executes a Task
   */
  public void executeTask(Message message) {
    logger_.info("WORKER: executeTask invoked");
    
    task_ = (Task)(message_.getContent());
       
    executionThread_ = new Thread(this);
    executionThread_.start();
    changeStatus(BUSY);
    message.setType(Message.EXECUTE_ACK);
  } // executeTask
  
  /**
   * This method prints the current state on the screen
   */
  public synchronized void printState(Message message) {
    logger_.info("WORKER: printState() invoked - " + status_);    

    switch (status_) {
      case FREE: logger_.info("WORKER: The current state is FREE"); break;
      case IDLE: logger_.info("WORKER: The current state is IDLE"); break;
      case BUSY: logger_.info("WORKER: The current state is BUSY"); break;
      case ENDING_SESSION :     
        logger_.info("WORKER: The current state is ENDING_SESSION"); break;
      default : logger_.severe("WORKER: Non valid state"); break;
    }
    //statistics_.currentTime() ;
    //systemInfo_[2] = statistics_.elapsedTimeStr_ ;
    
    message.setType(Message.STATUS_ACK) ; 
    message.setStatus(status_);
  } 

  /**
   * A end session message received in idle state
   */
  public void idleEndSession(Message message) {
    logger_.info("WORKER: idleEndSession() invoked");
    changeStatus(FREE);
    systemClassLoaderManager_ = null ;
    
    message.setType(Message.END_SESSION_ACK) ;
    message.setContent(null) ;
  } 

  /**
   * A end message received in busy state
   */
  public void busyEndSession(Message message) {
    logger_.info("WORKER: busyEndSession() invoked");
    changeStatus(ENDING_SESSION);
    systemClassLoaderManager_ = null ;
    
    message.setType(Message.END_SESSION_ACK) ;
    message.setContent(null) ;
  }   
  
  /**
   * this method changes the current state
   * @param status The new current status
   */
  public void changeStatus(int newStatus) {
    status_ = newStatus;
  } // ChangeStatus
  
  public void workUp() throws IOException {
    //JMetalReturnObject returnObject ;
    // TODO Auto-generated method stub

    // determine the ip of the receiver, we need it to complete the init data
    InetAddress inetAddress;
    String ip = null;
    try {
      inetAddress = InetAddress.getLocalHost();
      ip = inetAddress.getHostAddress();
    } catch (UnknownHostException ex) {
        ex.printStackTrace();
        System.exit(0);
    }
    while(!fin_ && !reset_) {
      try {
        // Waiting connection from driver
        logger_.info("WORKER. Waiting connection in " + ip + ":" + port_ + 
                     ". Status: " + status_) ;
        
        Socket socket = serverSocket_.accept() ;
        
        logger_.info("WORKER. Connection accepted " + socket.toString()) ;
        
        // Obtain the message
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()) ;
        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
        Object object = inStream.readObject() ;      

        message_ = (Message)object ;
        logger_.info("WORKER. received message: " + message_.getType()) ;
        // Create a empty message as response and decode the incoming message
        Message response = new Message(message_.getSessionId());
        response.setType(Message.ERROR); // Type by default. The method invoked should change this message
        Object [] params = {response};
        synchronized (this) {
          statusTable_[status_][message_.getType()].invoke(this,params);   
        }
        
        // Return the response to the other side
        outStream.writeObject(response);
        outStream.close();
        inStream.close();
        socket.close() ;
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(0);
      }              
    } // while
    if (fin_) {
      System.exit(0);
    } 
    else if (reset_) {
      serverSocket_.close() ;
      String command[] = {"java", "sparrow.worker.Worker", ""+ port_} ;
      Runtime.getRuntime().exec(command) ;
      System.exit(0) ;
    }
  } // workUp  
  
  /**
   * Code to execute by the executing thread
   */
  public void run() {
    System.out.println("BEGIN execute. Status: " + status_) ;
    Date startTime = new Date();
    CompletedTask completedTask = task_.execute(initData_);
    Date endTime = new Date();
    completedTask.setTime(endTime.getTime() - startTime.getTime()) ;
    System.out.println("END execute.   Status: " + status_) ;
    synchronized(this){
      try {
        if (status_ == ENDING_SESSION) {
          changeStatus(FREE);
          systemClassLoaderManager_ = null; 
        } else  {
          Socket socketResult = new Socket(initData_.getIP(), initData_.getPort());
          ObjectOutputStream outStreamA = new ObjectOutputStream(socketResult.getOutputStream());      
          ObjectInputStream inStreamA = new ObjectInputStream(socketResult.getInputStream());      
          Message message = new Message(sessionId_,Message.COMPLETED_TASK, completedTask);
          outStreamA.writeObject(message) ;
          outStreamA.close();
          inStreamA.close() ;
          socketResult.close();      
          changeStatus(IDLE);
        }
      } catch (Exception e) {
        logger_.info("WORKER: Exception raised in method run(): " + e);   
        logger_.info("WORKER: Changing to state IDLE");
        changeStatus(FREE);        
        systemClassLoaderManager_ = null ;
      }
    }
  }
  // run
  
  /**
   * @param args
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    logger_      = Logger.getLogger(Worker.class.getName());
    fileHandler_ = new FileHandler("worker.log");
    logger_.addHandler(fileHandler_) ;
    
    int port = (new Integer(args[0])).intValue();
    logger_.info("WORKER. Starting in port" + port) ; 
    Worker worker = new Worker(port);
    worker.workUp();
  } // main
}
