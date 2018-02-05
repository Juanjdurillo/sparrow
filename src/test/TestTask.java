/**
 * TestTask.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
package test;

import sparrow.util.CompletedTask;
import sparrow.util.Task;

public class TestTask extends Task {
  double [] array_ ;
  
  /**
   * Constructor
   */
  public TestTask(double [] array) {
    array_ = array ;
  } // TestTask
  
  /**
   * Executes the method that performs the task
   * @param parameters Parameters
   */
  public CompletedTask execute(Object parameters) {
    double sum ;
    sum = 0 ;
    
    for (int i = 0; i < array_.length; i++)
      sum += array_[i] ;
    
    try {
      System.out.println("Thread " + getId() + " sleeping") ;
      Thread.sleep(5000) ;
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return new TestCompletedTask(getId(), sum) ;
  } // CompletedTask
}
