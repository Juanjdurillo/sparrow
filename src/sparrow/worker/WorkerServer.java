/**
 * WorkersServer.java
 * @author Juan J. Durillo
 */

package sparrow.worker;

import sparrow.util.Message;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This class implements a server that provides the available workers
 */
public class WorkerServer extends Thread {
  /**
   * Logger and file handler
   */
  private static Logger      logger_ ;
  private static FileHandler fileHandler_ ;
  
  /**
   * stores the file in which workers are stored
   */
  private String file_;
  
  /**
   * stores the port in which WorkersServer are waiting
   */
  int port_;
  
  /**
   * stores a serverSocket
   */
  private ServerSocket serverSocket_;
  
  /**
   * stores all the workers in the File
   */
  private List<WorkerId> allTheWorkers_;
  
  /**
   * stores all the availability workers
   */
  private List<WorkerId> workersAvailable_;
  
  /** 
   * stores a Mutex for synchronizated operations
   */
  private Object mutex_;
  
  /**
   * status table
   */
  private Method [] statusTable_;
  
  /**
   * stores the last received messages
   */
  private Message message_;
  
  /**
   * Creates a WorkerServer
   */
  public WorkerServer(String file, int port) {
    logger_.info("WORKERS_SERVER: Starting execution");
    
    file_ = file;
    workersAvailable_ = new LinkedList<WorkerId>();
    mutex_ = new Object();
    port_  = port;
    
    try {
      serverSocket_ = new ServerSocket(port_);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
    // Defines the machine status for this this server
    // DEFINE HERE THE STATUS MACHINE FOR THE WORKER
    statusTable_ = new Method[2]; // Three state, three messages
   
    Class [] paramList = new Class[1];
    paramList[0] = Message.class;
    try {
      statusTable_[Message.GET_ALL] = WorkerServer.class.getMethod("getAll",paramList);
      statusTable_[Message.GET_N]   = WorkerServer.class.getMethod("getN",paramList);
    } catch (SecurityException ex) {
      ex.printStackTrace();
    } catch (NoSuchMethodException ex) {
      ex.printStackTrace();
    }
  } // WorkersServer
  
  /**
   * Creates a message with all the available workers, and stores them
   * in the assigned workers list
   */
  public void getAll(Message response) {
    logger_.info("WORKERS_SERVER: getAll() invoked");    
    // Mutual exclusion
    synchronized(mutex_) {
      WorkerId [] aux = new WorkerId[workersAvailable_.size()];
      Iterator<WorkerId> iterator = workersAvailable_.iterator();
      int i = 0;
      while (iterator.hasNext()) {
        WorkerId worker = iterator.next();
        aux[i++] = worker;
        iterator.remove();
      }
      response.setType(Message.ACK);
      response.setContent(aux);
    }
  } // getAll
  

  /**
   * Creates a message_ with the maximun number of available workers closer
   * to n, and stores it in the assigned workers list
   */
  public void getN(Message response) {
    logger_.info("WORKERS_SERVER: getN() invoked");      
    // Mutual exclusion
    synchronized(mutex_) {
      int n = ((Integer)message_.getContent()).intValue();
      n = Math.min(n,workersAvailable_.size());
      
      WorkerId [] aux = new WorkerId[workersAvailable_.size()];
      //Iterator<WorkerId> iterator = workersAvailable_.iterator();
      for (int i = 0; i < n; i++) {
        WorkerId worker = workersAvailable_.get(0);
        workersAvailable_.remove(0);
        aux[i] = worker;
        //iterator.remove();
      }
      response.setType(Message.ACK);
      response.setContent(aux);
    }
  } // getAll
  
  
  
  
  
  
  /**
  * This method defines the behavior of the WorkersServer
  */
  public void workUp() {

    while(true) {
      try {
        // Waiting connection from driver
        logger_.info("WORKERS_SERVER: Waiting connection in port " + port_);        
        Socket socket = serverSocket_.accept() ;
        
        //logger_.info("WorkersServer. Connection accepted " + socket.toString()) ;
        
        // Obtain the message_
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()) ;
        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
        Object object = inStream.readObject() ;      

        message_ = (Message)object ;
        //logger_.fine("WorkersServer. received message: " + message_.getType()) ;
        // Create a empty message_ as response and decode de incoming message_
        Message response = new Message(-1);
        response.setType(Message.ERROR); // Type by default. The method invoked should change this message_
        Object [] params = {response};
        logger_.info("WORKERS_SERVER: Received message " + message_.getType()); 
        statusTable_[message_.getType()].invoke(this,params);   
        
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
  } // workUp  
  
  /**
   * Checks which workers are available
   */
  public void checkAvailability()  {
    logger_.info("WORKERS_SERVER: checkAvailability() invoked");     
    
    // We build a message_ for check the status of all the nodes
    Message statusMessage = new Message(-1);
    statusMessage.setType(Message.STATUS);
  
    Message response; // To store responses
    
    // Iterate 
    Iterator<WorkerId> iterator = allTheWorkers_.iterator();
    while (iterator.hasNext()) {
      Socket socket = null ;
      WorkerId worker= iterator.next();
      try {
          
        
        InetAddress in = InetAddress.getByName(worker.getIP());
        /*
         * This part of code try to reach the worker using the UNIX ping
         * command. Some problems have been appreciated in Mac OX 10.5
        Runtime r = null;
        Process p = null;
        r = Runtime.getRuntime();
        p = r.exec("ping -q -c 2 -w 2 " + worker.getIP());
        try { 
        p.waitFor();
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (p.exitValue() == 0) {
        */
        InetAddress remoteHost = InetAddress.getByName(worker.getIP());
        if (remoteHost.isReachable(2000)) { // maximum of two seconds waiting
          socket = new Socket(worker.getIP(),worker.getPort());
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream   = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;
          response = (Message)inStream.readObject();      
          inStream.close();
          outStream.close();
          socket.close();
          if (response.getType() == Message.STATUS_ACK) {
            switch (response.getStatus()) {
              case Worker.FREE: 
                {
                  if (!workersAvailable_.contains(worker)) {
                    synchronized (mutex_) {
                      workersAvailable_.add(worker);
                    }
                  }
                }
                break;
              default : 
                {
                  if (workersAvailable_.contains(worker)) {
                    synchronized (mutex_) {
                      workersAvailable_.remove(worker); 
                    }
                  }
                }
              break;
            }
          }       
        } else {
          if (workersAvailable_.contains(worker)) {
            synchronized (mutex_) {
              workersAvailable_.remove(worker);
            }
          }
        }
      } catch (ConnectException exception) {
        logger_.info("WORKERS_SERVER: cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (UnknownHostException exception2) {
        logger_.severe("WORKERS_SERVER: cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {  
        logger_.severe("WORKERS_SERVER: cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (ClassNotFoundException e) {
        System.err.println(e);
        e.printStackTrace();
      } finally {
        if (socket!= null)
          try {
            socket.close();
          } catch (IOException e) {
            logger_.severe("WORKERS_SERVER: exception in socket" + socket);
          }
      }
    }  
  } // checkAvailability
    
  /**
   * Reads the information about the workers from a file
   * @param fileName File containing pairs (IP, port)
   * @return The number of read workers
   * @throws IOException 
   */
  public int readWorkerIds() throws IOException {
    logger_.info("WORKERS_SERVER: readWorkerIds() invoked");  
    
    int counter ;

    counter = 0 ;
    allTheWorkers_ = new ArrayList<WorkerId>();
    //FileInputStream file = new FileInputStream(fileName); 

    StringTokenizer st;
    String line;
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(file_)));
      while ((line = bufferedReader.readLine()) != null) {
        st = new StringTokenizer(line, " ");
        String IP      = st.nextToken() ;
        String portStr = st.nextToken() ;
        allTheWorkers_.add(new WorkerId(IP,(new Integer(portStr)).intValue())); 
        // or put in data structure
        System.out.println(IP + " " + portStr);
      }
      bufferedReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return counter ;
  } // readWorkerIds
  
    
  /**
   * redefines the run method
   */
  public void run() {
    try {
      readWorkerIds();
      checkAvailability();
      while (true) {
        Thread.sleep(15000); //Wait for 15 seconds
        readWorkerIds();
        checkAvailability();
      }
    } catch (Exception e) {
      //logger_.warning("An execution have been happened :"+e);
      e.printStackTrace();
    }
  } // run
  
  // This method launches the WorkersServer
  public static void main(String [] args) throws SecurityException, IOException {
    logger_      = Logger.getLogger(Worker.class.getName());
    fileHandler_ = new FileHandler("workersServer.log");
    logger_.addHandler(fileHandler_) ;
    
    if (args.length < 2) {
      logger_.severe("Number of arguments invalid") ;
      logger_.severe("Usage: java sparrow.worker.WorkersServer file port") ;
      System.exit(-1) ;
    } // if
    
    WorkerServer ws = new WorkerServer(args[0],new Integer(args[1]));
    Thread t = new Thread(ws);
    // Start the behabour of the server
    t.start();
    ws.workUp() ;
  } // main

  
} // WorkersServer
