/**
 * Control.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 */
package sparrow.driver;

import java.io.* ;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import sparrow.util.Message;
import sparrow.util.SharedObjects;
import sparrow.util.Task;
import sparrow.util.WorkerStatistics;
import sparrow.worker.Worker;
import sparrow.worker.WorkerId;

public class Control extends Thread {
   
  /** 
   * All the known workers
   */
  //private List<WorkerId> allWorkers_;
  
  /**
   * stores the objects shared 
   */
  private SharedObjects sharedObjects_;
  
  /**
   * stores the port in which the receiver wait
   */
  int port_;
  
  /**
   * stores the IP y port of the workersServer
   */
  int workersServerPort_;
  String workersServerIP_;
  
  /**
   * stores the number of new machines of the system
   */
  int newMachines_ = 0;
  
  /**
   * stores the the maximum number of nodes needed_
   */
  int maximunNodes_ = -1;
  
  /**
   * Create a new Control
   */
  public Control(SharedObjects sharedObjects, 
                 int           receiverPort, 
                 int           workersServerPort, 
                 String        workersServerIP) {
    sharedObjects_ = sharedObjects;
    workersServerPort_ = workersServerPort;
    workersServerIP_ = workersServerIP;
    port_ = receiverPort;
    
  } // Control
  
  /**
   * Checks the state for all the known nodes
   */
  public void initStatus()  {
    
    // We build a message to check the status of all the nodes
    Message statusMessage = new Message(sharedObjects_.sessionId_);
    statusMessage.setType(Message.STATUS);
    
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
    sharedObjects_.initData_.setIP(ip);
    sharedObjects_.initData_.setPort(port_);
    
    // We build a message with the URLs to be sent to the workers
    Message urlsMessage = new Message(sharedObjects_.sessionId_,
                                      Message.INIT_SESSION,
                                      sharedObjects_.initData_.getURLs()) ;
    // We build a message with the init data
    Message initMessage = new Message(sharedObjects_.sessionId_,
                                      Message.INIT_SESSION,
                                      sharedObjects_.initData_);
    Message response; // To store responses
    
    // Iterate 
    Iterator<WorkerId> iterator = sharedObjects_.allWorkers_.iterator();
    while (iterator.hasNext()) {
      Socket socket = null ;
      WorkerId worker= iterator.next();
      try {
        InetAddress in = InetAddress.getByName(worker.getIP());
        if (in.isReachable(1800)) {
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
              socket = new Socket(worker.getIP(),worker.getPort());
              ObjectOutputStream outStream2 = new ObjectOutputStream(socket.getOutputStream()) ;
              ObjectInputStream inStream2   = new ObjectInputStream(socket.getInputStream()) ;
              
              outStream2.writeObject(urlsMessage) ;
              response = (Message)inStream2.readObject();   

              inStream2.close();
              outStream2.close();
              socket.close();
              
              socket = new Socket(worker.getIP(),worker.getPort());
              outStream2 = new ObjectOutputStream(socket.getOutputStream()) ;
              inStream2   = new ObjectInputStream(socket.getInputStream()) ;

              outStream2.writeObject(initMessage) ;
              response = (Message)inStream2.readObject();   
              
              if (response.getType()==Message.INIT_SESSION_ACK) {
                System.out.println("Worker " + worker + " answered OK");
                sharedObjects_.idleWorkers_.add(worker);
                WorkerStatistics statistics;
                statistics = sharedObjects_.differentWorkers_.get(worker);
                if (statistics == null) {
                  statistics = new WorkerStatistics();
                  sharedObjects_.differentWorkers_.put(worker,statistics);
                }
                //statistics.updateStatistics(WorkerStatistics.Key.START_SESSION,null);
              } else {
                iterator.remove();
              }
              inStream2.close();
              outStream2.close();
              socket.close();
            }
              break;
            default : 
              {
                System.out.println("jMetalDriver-> worker "+ worker + "cannot be adquired");
                //iterator.remove(); 
              }
            break;
          }
        }       
       } else {
          iterator.remove();
       }
      }catch (ConnectException exception) {
        System.err.println("jMetalDriver-> cannon connect to worker "+worker) ;
        iterator.remove();
      } catch (UnknownHostException exception2) {
        System.err.println("jMetalDriver-> cannon connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {  
        System.err.println("jMetalDriver-> cannon connect to worker "+worker) ;
        iterator.remove();
      } catch (ClassNotFoundException e) {
        System.err.println(e);
        e.printStackTrace();
      } finally {
        if (socket!= null)
          try {
            socket.close();
          } catch (IOException e) {
            System.out.println("jMetalDriver-> exception in socket" + socket);
          }
         
      }
    }    
  } // initStatus
  
   /**
   * Prints statistics of the workers
   */
  public void statistics(Collection<WorkerStatistics> collection) {
    Message statusMessage = new Message(0);
    statusMessage.setType(Message.STATUS);  
    Message response; // To store responses
    
    WorkerStatistics statistics =  null ;
        
    int  totalComputedTasks     = 0 ;
    int  totalNodes             = 0 ;
    int  totalTimeComputedTasks = 0 ;
    int  totalBenchmark         = 0 ;
    long maximumBenchmark       = 0 ;
    long minimumBenchmark       = Integer.MAX_VALUE ;
    
    // Iterate 
    Iterator<WorkerStatistics> iterator = collection.iterator();
    System.out.print  ("--------------------------------------") ;
    System.out.println("--------------------------------------") ;
    System.out.println("Worker \t\t\tTasks \tSess \tCompTime \tUpTime") ;
    System.out.print  ("--------------------------------------") ;
    System.out.println("--------------------------------------") ;
    while (iterator.hasNext()) {
      statistics = iterator.next() ;
      //String properties[] = (String[])response.getContent() ;
      System.out.println("\t"+statistics.totalNumberOfComputedTasks_ + 
                         //"\t" + statistics.numberOfSessions_+ 
                         "\t" + statistics.DDHHMMSS(statistics.totalComputingTime_)) ; 
                         //+ "\t\t" + statistics.elapsedTimeStr_) ;

      totalNodes ++ ;
      totalComputedTasks += statistics.totalNumberOfComputedTasks_ ;
      totalTimeComputedTasks += statistics.totalComputingTime_ ;
      /*
      if (statistics.benchmark_ > maximumBenchmark)
        maximumBenchmark = statistics.benchmark_ ;
      if (statistics.benchmark_ < minimumBenchmark)
        minimumBenchmark = statistics.benchmark_ ;

        totalBenchmark += statistics.benchmark_ ;
        */
    } 
      
    System.out.println("\nSUMMARY:") ;
    System.out.println("---------------------------------------------") ;
    System.out.println("Total nodes                  : " + totalNodes);
    System.out.println("Total computed tasks         : " + totalComputedTasks);
    System.out.println("Total time computed tasks    : " + 
                        statistics.DDHHMMSS(totalTimeComputedTasks));
    long wallClockTime = sharedObjects_.endTime_.getTime() - 
                         sharedObjects_.startTime_.getTime() ;
    String wallClockTimeStr = WorkerStatistics.DDHHMMSS(wallClockTime) ;
    System.out.println("Wall clock time              : " + wallClockTimeStr) ;
    System.out.println("Time reduction               : " + 
        1.0*totalTimeComputedTasks / wallClockTime) ;
    
    double averageBenchmark = 1.0 * totalBenchmark / totalNodes ;
    System.out.println() ;
    System.out.println("Minimum benchmark factor     : " + minimumBenchmark) ;
    System.out.println("Maximum benchmark factor     : " + maximumBenchmark) ;
    System.out.println("Average benchmark factor     : " + averageBenchmark) ;
    System.out.println() ;
    //System.out.println("Estimated parallel performace: " + 
    //    1.0*totalTimeComputedTasks/totalNodes) ;
    //System.out.println("Average parallel performace  : " + 
    //    (1.0*totalTimeComputedTasks/totalNodes)*(1.0*averageBenchmark/maximumBenchmark)) ;
  }

  
  /**
   * Checks the status of all the workers
   */
  public void checkStatus() throws Exception {
    // Build the message (We need only a message for all the nodes_) 
    Message statusMessage = new Message(0);
    statusMessage.setType(Message.STATUS);  
    Message response; // To store responses
    int free = 0, idle = 0, busy = 0, total = 0;
    
    
    // Iterate
    Iterator<Integer> iterator = sharedObjects_.busyWorkers_.keys();
    while (iterator.hasNext()) {
      Integer idTask = iterator.next();
      WorkerId worker = null;
      try {
        worker =  sharedObjects_.busyWorkers_.get(idTask);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (worker!= null) {
        Socket socket = null;
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;
        try {
          InetAddress remoteHost = InetAddress.getByName(worker.getIP());
          if (remoteHost.isReachable(1800)) {
            socket = new Socket(worker.getIP(),worker.getPort());
            outStream = new ObjectOutputStream(socket.getOutputStream()) ;
            inStream   = new ObjectInputStream(socket.getInputStream()) ;
            outStream.writeObject(statusMessage) ;
            response = (Message)inStream.readObject();      
            if (response.getType() != Message.STATUS_ACK) {
              Task task = sharedObjects_.scheduledTask_.get(idTask);
              if (task!=null) {
                sharedObjects_.scheduledTask_.remove(idTask);
                sharedObjects_.nonScheduledTask_.add(task);
                sharedObjects_.busyWorkers_.remove(idTask);
                sharedObjects_.allWorkers_.remove(worker);
              }
            }
            inStream.close();
            outStream.close();
            socket.close();
          }
        } catch (ClassNotFoundException e) {
          System.err.println(e);
          e.printStackTrace();
        }
        catch (Exception exception) {
          System.err.println("WORKERS_SERVER-> cannon connect to worker "+worker) ;
          Task task = sharedObjects_.scheduledTask_.get(idTask);
          if (task!=null) {
            sharedObjects_.scheduledTask_.remove(idTask);
            sharedObjects_.nonScheduledTask_.add(task);
            sharedObjects_.busyWorkers_.remove(idTask);
            sharedObjects_.allWorkers_.remove(worker);
          }
          System.out.println("The worker "+ worker.getIP() + " " + worker.getPort() + " has been removed");
        } finally {
          try {
            if (inStream!=null)
              inStream.close();
            if (outStream!=null)
              outStream.close();
            if (socket!=null)
              socket.close();
          } catch (IOException e) {
            System.err.println("WORKERS_SERVER-> Exception in socket " + socket);
          }
        }
      }
    }
  } // checkStatus
  
  /**
   * Ask for all the available workers
   *
   */
  public int askForNodes() {
    int neededWorkers = maximunNodes_ - sharedObjects_.allWorkers_.size();
    int news = 0;
    Socket socket = null;
    if ((neededWorkers > 0) || (maximunNodes_ == -1)) {
      // We build a message for check the status of all the nodes
      Message statusMessage = new Message(sharedObjects_.sessionId_);
      if (maximunNodes_ == -1) {
        statusMessage.setType(Message.GET_ALL);
      } else {
        statusMessage.setType(Message.GET_N);
      }
      statusMessage.setContent(neededWorkers);
      Message response; // To store responses
      // Iterate 
      try {
        socket = new Socket(workersServerIP_,workersServerPort_);
        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
        ObjectInputStream inStream   = new ObjectInputStream(socket.getInputStream()) ;
        outStream.writeObject(statusMessage) ;
        response = (Message)inStream.readObject();      
        inStream.close();
        outStream.close();
        socket.close();
        if (response.getType() == Message.ACK) {
          WorkerId [] aux = (WorkerId[])response.getContent();
          for (int i = 0; i < aux.length; i++) {
            sharedObjects_.allWorkers_.add(aux[i]);
            news++;
          }
        }       
      }catch (Exception exception) {
        exception.printStackTrace();
      } finally {
        if (socket!= null)
          try {
            socket.close();
          } catch (IOException e) {
            System.out.println("WORKERS_SERVER-> exception in socket" + socket);
          }  
      }
    } 
    return news;
  } // askForNode

  public void endSession()  {
    System.out.println("WORKERS_SERVER: endWorkerSessions() invoked"); 
    
    // We build a message_ to check the status of all the nodes
    Message statusMessage = new Message(Message.END_SESSION);
    //statusMessage.setType(Message.STATUS);
    statusMessage.setType(Message.END_SESSION);
    
    Message response; // To store responses
    
    //Iterator<WorkerId> iterator = sharedObjects_.allWorkers_.iterator();
    Iterator<WorkerId> iterator = sharedObjects_.differentWorkers_.keySet().iterator();
    while (iterator.hasNext()) {
      Socket socket = null ;
      WorkerId worker= iterator.next();
      System.out.println("Sending end session to: "+worker);
          
      try {
        InetAddress in = InetAddress.getByName(worker.getIP());
        if (in.isReachable(1800)) {
          socket = new Socket(worker.getIP(),worker.getPort());
          
          System.out.println("Sending end session to: "+worker);
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream   = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;
          response = (Message)inStream.readObject();      
          inStream.close();
          outStream.close();
          socket.close();
        }
      } catch (ConnectException exception) {
        //logger_.info("WORKERS_SERVER: cannot connect to worker "+worker) ;
        System.out.println("WORKERS_SERVER: cannot connect to worker" + worker);
      } catch (UnknownHostException exception2) {
        //logger_.severe("WORKERS_SERVER: cannot connect to worker "+worker) ;
        System.out.println("WORKERS_SERVER: cannot connect to worker " + worker);
      } catch (IOException exception3) {  
        //logger_.severe("WORKERS_SERVER: cannot connect to worker "+worker) ;
        System.out.println("WORKERS_SERVER: cannot connect to worker " + worker);
      } catch (ClassNotFoundException e) {
        System.err.println(e);
        e.printStackTrace();
      } finally {
        if (socket!= null)
          try {
            socket.close();
          } catch (IOException e) {
            System.out.println("WORKERS_SERVER-> exception in socket" + socket);
          }         
      }
      
      // Update the statics for each worker
      //WorkerStatistics statistics = sharedObjects_.differentWorkers_.get(worker);
      //if (statistics!=null) {
      //  statistics.updateStatistics(WorkerStatistics.Key.END_SESSION,null) ;
      //}
    }     
  } // endSession
  
  /**
   * Returns the total number of machines *
   */
  public int getTotal() {
    return sharedObjects_.allWorkers_.size();
  } // getTotal
  
  /**
   * Returns the number of new machines and set it to zero
   */
  public int getNewMachines(){
    int result   = newMachines_;
    newMachines_ = 0;
    return result;
  } // getNewMachines
  
  /**
   * Maximum number of machines needed
   */
  public void setMaximum(int n) {
    maximunNodes_ = n;
  } // setMaximum
  
  
  /**
   * This thread implements the fail tolerance of the control thread
   */
  public void run() {
    int count = 0;
    try {
      askForNodes();
      initStatus();
    } catch (Exception e) {
      System.err.println("Sparrow error: " + e);
    }
    while (!sharedObjects_.terminate_) {
      try {
        Thread.sleep(1000);
        if ((count == 30) &&
            (!sharedObjects_.terminate_)){
          checkStatus();
          count = 0;
        }
        count++;
        newMachines_ += askForNodes();
        if (!sharedObjects_.terminate_) {
          initStatus();
        }
      } catch (Exception e) {
        System.err.println("Sparrow error: " + e );
        e.printStackTrace();
      }
    }
  } // run
  
  
} // Control



