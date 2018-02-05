/**
 * WorkerId.java
 * @author Juan J. Durillo
 */
package sparrow.worker;

import java.io.Serializable;

/** 
 * This class are use to have a representation of the worker in the driver side
 */
public class WorkerId implements Serializable{
  
  /*
   * stores the IP of the worker
   */
  private String IP_;
  
  /*
   * stores the port in which the worker are listening
   */
  private int port_;
  
  /*
   * stores the current state of this worker
   */
  private int status_;
  
  /*
   * stores the id of the taks currently in execution by the worker.
   * only for the state execute and terminting
   */
  private int id_;
  
  /**
   * Creates a new WorkerId
   */
  public WorkerId(String IP, int port) {
    IP_   = IP;
    port_ = port;
  } // WorkerId
  
  
  /**
   * Return the worker IP
   */
  public String getIP() {
    return IP_;
  } // getIP
  
  /**
   * Return the worker port
   */
  public int getPort() {
    return port_;
  } // getPort
  
  
  /**
   * Return the worker status
   */
  public int getStatus() {
    return status_;
  } // getStatus
  
  /**
   * Sets the worker status
   */
  public void setStatus(int status) {
    status_ = status;
  } // setStatus
  
  /**
   * Return the worker task id 
   */
  public int getId() {
    return id_;
  } // getId
  
  
  /**
   * Sets the worker id
   */
  public void setId(int id) {
    id_ = id;
  } // setId
  
  
  /**
   * Return true if this and the another worker are equals
   * False in other case
   */
  public boolean equals(Object another) {
    WorkerId anotherWorker = (WorkerId)another;
    return IP_.equals(anotherWorker.getIP()) && 
           port_ == anotherWorker.getPort();
  } // equals
  
  public String toString() {
    return IP_ + ":" + port_;
  }

} // WorkerId
