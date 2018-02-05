/**
 * Task.java
 *
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 */
package sparrow.util;

/**
 * This class implements the generic behavior for all the tasks
 */
import java.io.Serializable;

public abstract class Task implements Serializable {
 
  /**
   * for assig the id_
   */
  public static int count_ = 0;
  
  /**
   * stores the identifier of this task
   */
  private int id_ ;
  
  /**
   * 
   * Creates a new Task
   */
  public Task(){
    id_ = count_++;;
  } // Task
   
  /**
   * gets the id of the task
   */
  public int getId() {
    return id_;
  } // getId
	
  /**
   * this is executed by the worker
   */
  public abstract CompletedTask execute(Object param);
}
