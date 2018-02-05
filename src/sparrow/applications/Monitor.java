/**
 * Monitor.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 */
package sparrow.applications;

import java.io.* ;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

//import javax.management.MXBean;
//import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
import sparrow.driver.Control;
import sparrow.util.Message;
import sparrow.util.WorkerStatistics;
import sparrow.worker.Worker;
import sparrow.worker.WorkerId;

/**
 * This class implements an application to show how many workers there are
 * in the system and which status have these workers.
 */
public class Monitor{

  /** 
   * stores all the known workers
   */
  private List<WorkerId> allWorkers_;
  /**
   *

  /** 
   * Creates a new Monitor
   */
  public Monitor() {

  } // Monitor 

  /**
   * print the status of each worker
   */
  public void printStatus() {
    // Build the message (We need only a message for all the nodes_) 
    //OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean() ;
    //System.out.println("Arquitectura: " + bean.getArch()) ;
    //System.out.println("Processors  : " + bean.getAvailableProcessors()) ;
    //System.out.println("Op. System  : " + bean.getName()) ;
    //System.out.println("Version     : " + bean.getVersion()) ;
    //   System.out.println("Load Aver.  : " + bean.getSystemLoadAverage()) ;

    Message statusMessage = new Message(0);
    statusMessage.setType(Message.STATUS);  
    Message response; // To store responses
    int free = 0, idle = 0, busy = 0, endingSession = 0, total = 0 ;
    int uncontacted = 0 ;

    // Iterate 
    Iterator<WorkerId> iterator = allWorkers_.iterator();
    System.out.print  ("--------------------------------------") ;
    System.out.println("--------------------------------------") ;
    System.out.println("Worker \t\t\t Status \tOpSystem \tArch \tTime") ;
    System.out.print  ("--------------------------------------") ;
    System.out.println("--------------------------------------") ;
    while (iterator.hasNext()) {
      Socket socket = null;
      WorkerId worker= iterator.next();
      System.out.print(worker.getIP() + "(" + worker.getPort()+") \t " ) ;
      try {
        InetAddress remoteHost = InetAddress.getByName(worker.getIP());
        if (remoteHost.isReachable(1800)) {
          socket = new Socket(worker.getIP(),worker.getPort());
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream   = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;
          response = (Message)inStream.readObject();      
          if (response.getType() == Message.STATUS_ACK) {
            switch (response.getStatus()) {
            case Worker.FREE:        
            {
              free++;
              total++;
              System.out.print("FREE\t") ;
            }
            break;
            case Worker.IDLE:        
            {
              idle++;
              total++;
              System.out.print("IDLE\t") ;
            } 
            break;
            case Worker.BUSY:        
            {
              busy++;
              total++;
              System.out.print("BUSY\t") ;            
            }  
            break;
            case Worker.ENDING_SESSION: 
            {
              endingSession++;
              total++;
              System.out.print("ENDING_SESSION") ;
            }  
////        break;
            default : 
            {
              System.out.println("Monitor-> worker "+ worker + "cannot be adquired");
              //iterator.remove(); 
              //uncontacted++;
              //System.out.println("UNCONTACTED") ;            
            }
            break;
            } // switch
            //WorkerStatistics statistics ;
            //statistics = (WorkerStatistics)response.getContent() ;
            //String properties[] = (String[])response.getContent() ;
            //System.out.println("\t" + statistics.operatingSystem_ + 
            //                   "\t" + statistics.architecture_ +
            //                   "\t" + statistics.elapsedTimeStr_) ;
            System.out.println();
          } // if
          inStream.close();
          outStream.close();
          socket.close();
        }
      } 
      catch (ConnectException exception) {
        //System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        //iterator.remove();
        uncontacted++;
        System.out.println("UNCONTACTED") ;   
      } catch (UnknownHostException exception2) {
        System.err.println("Monitor-> cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {
        System.err.println("Monitor-> cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (ClassNotFoundException e) {
        System.err.println(e);
        e.printStackTrace();
      } //finally {
      //try {
      //  socket.close();
      //} catch (IOException e) {
      //  System.err.println("jMetalDriver-> Exception in socket " + socket);
      //}
      //}
    }

    System.out.println("\nSUMMARY:") ;
    System.out.println("--------------------------") ;
    System.out.println("Total Nodes   : " + total);
    System.out.println("Free          : " + free);
    System.out.println("Idle          : " + idle);
    System.out.println("Busy          : " + busy);
    System.out.println("EndingSession : " + endingSession);
    System.out.println() ;
    System.out.println("Uncontacted : " + uncontacted);
  } // printStatus

  /**
   * Reads the information about the workers from a file
   * @param fileName File containing pairs (IP, port)
   * @return The number of read workers
   * @throws IOException 
   */
  public int readWorkerIds(String fileName) throws IOException {
    int counter ;

    counter = 0 ;
    allWorkers_ = new ArrayList<WorkerId>();
    //FileInputStream file = new FileInputStream(fileName); 

    StringTokenizer st;
    String line;
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
      while ((line = bufferedReader.readLine()) != null) {
        st = new StringTokenizer(line, " ");
        String IP      = st.nextToken() ;
        String portStr = st.nextToken() ;
        allWorkers_.add(new WorkerId(IP,(new Integer(portStr)).intValue())); 
        // or put in data structure
        //System.out.println(IP + " " + portStr);
      }
      bufferedReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return counter ;
  }

  /**
   * Sends reset to all the workers 
   */
  public static void reset(List<WorkerId> workersToReset) {
    Message statusMessage = new Message(0);
    statusMessage.setType(Message.RESET);  

    // Iterate 
    Iterator<WorkerId> iterator = workersToReset.iterator();
    while (iterator.hasNext()) {
      Socket socket = null;
      WorkerId worker= iterator.next();
      //System.out.print(worker.getIP() + "(" + worker.getPort()+") \t " ) ;
      try {
        InetAddress remoteHost = InetAddress.getByName(worker.getIP());
        if (remoteHost.isReachable(1800)) {        
          socket = new Socket(worker.getIP(),worker.getPort());
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream   = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;

          Message ack ;
          ack = (Message)inStream.readObject();      
          if (ack.getType() == Message.RESET_ACK) {
            System.out.println("RESET_ACK") ;
          }
          inStream.close();
          outStream.close();
          socket.close();
        }
      } catch (ConnectException exception) {
        //System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        //iterator.remove();
      } catch (UnknownHostException exception2) {
        System.err.println("MONITOR: cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {
        System.err.println("MONITOR: cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Sends reset to all the workers 
   */
  public void reset() {
    Message statusMessage = new Message(0);
    statusMessage.setType(Message.RESET);  
    Iterator<WorkerId> iterator = allWorkers_.iterator();
    Message response; // To store responses

    while (iterator.hasNext()) {
      Socket socket = null;
      WorkerId worker= iterator.next();
      System.out.print("Reseting " + 
          worker.getIP() + "(" + worker.getPort()+") \t " ) ;
      try {
        InetAddress remoteHost = InetAddress.getByName(worker.getIP());
        if (remoteHost.isReachable(1800)) {
          socket = new Socket(worker.getIP(),worker.getPort());
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;
          response = (Message)inStream.readObject();      
          if (response.getType() == Message.RESET_ACK) {
            System.out.println("OK") ;
          } // if
          else
            System.out.println("Error receiving RESET_ACK") ;
          outStream.close();
          inStream.close();
          socket.close();
        } else {
          System.out.println();
        }
      } 
      catch (ClassNotFoundException exception) {
        //System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        //iterator.remove();
        System.out.println("Class not found") ;   
      } catch (ConnectException exception) {
        System.out.println("UNCONTACTED") ;   
      } catch (UnknownHostException exception2) {
        System.err.println("MONITOR-> cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {
        System.err.println("MONITOR-> cannot connect to worker "+worker) ;
        iterator.remove();
      } 
    }
  }

  /**
   * Show statistics for all the workers
   */
  public void statistics() {
    //Control.statistics(allWorkers_);
    System.err.println("Monitor. Method 'statistics' is not implemented") ;
  }

  /**
   * Terminates all the workers
   */
  public void terminate() {
    Message statusMessage = new Message(0);
    statusMessage.setType(Message.TERMINATE);  
    Iterator<WorkerId> iterator = allWorkers_.iterator();
    Message response; // To store responses

    while (iterator.hasNext()) {
      Socket socket = null;
      WorkerId worker= iterator.next();
      System.out.print("Terminating " + 
          worker.getIP() + "(" + worker.getPort()+") \t " ) ;
      try {
        InetAddress remoteHost = InetAddress.getByName(worker.getIP());
        if (remoteHost.isReachable(1800)) {
          socket = new Socket(worker.getIP(),worker.getPort());
          ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream()) ;
          ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream()) ;
          outStream.writeObject(statusMessage) ;
          response = (Message)inStream.readObject();      
          if (response.getType() == Message.TERMINATE_ACK) {
            System.out.println("OK") ;
          } // if
          else
            System.out.println("Error receiving TERMINATE_ACK") ;
          outStream.close();
          inStream.close();
          socket.close();
        } else {
          System.out.println();
        }
      } 
      catch (ClassNotFoundException exception) {
        //System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        //iterator.remove();
        System.out.println("Class not found") ;   
      } catch (ConnectException exception) {
        System.out.println("UNCONTACTED") ;   
      } catch (UnknownHostException exception2) {
        System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        iterator.remove();
      } catch (IOException exception3) {
        System.err.println("jMetalDriver-> cannot connect to worker "+worker) ;
        iterator.remove();
      } 
    }
  } // terminate

  /**
   * Checks the status for all the nodes
   */
  public static void main(String [] args) {
    try {
      Monitor monitor = null;
      monitor = new Monitor();
      monitor.readWorkerIds("workers.txt");

      if ((args.length == 0) || (args[0].equals("status")))
        monitor.printStatus();
      else if (args[0].equals("reset")) 
        monitor.reset() ;
      else if (args[0].equals("statistics"))
        monitor.statistics() ;
      else if (args[0].equals("terminate"))
        monitor.terminate() ;

    } catch (Exception e) {
      System.err.println("JMetalD Monitor have been crashed");
      e.printStackTrace();
    }
  } // main
} // Monitor



