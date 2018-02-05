/**
 * CompletedTask.java
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 */
package sparrow.util;

import java.io.Serializable;

public class CompletedTask implements Serializable{
	
  /**
   * stores the identifier of the task
   */
  private int id_ ;
  
  /**
   * stores the computing time of the executed task 
   */
  private long time_;
  
  /**
   * creates a new instance
   */
  public CompletedTask(int id) {
    id_ = id;
  } // CompletedTask
  
  /**
   * get the id of the task
   */
  public int getId() {
    return id_;
  } // getId
  
  /**
   * sets the time needed to compute the completed task
   * @param time The time
   */
  public void setTime(long time) {
    time_ = time;
  } // setTime
  
  /**
   * gets the time needed to computed the completed task
   * @return The computing time
   */
  public long getTime() {
    return time_;
  } // getTime
} // CompletedTask
