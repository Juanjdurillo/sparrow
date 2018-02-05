/**
 * TestCompletedTask.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */

package test;

import java.io.Serializable;

import sparrow.util.CompletedTask;

public class TestCompletedTask extends CompletedTask {
  public double value_ ;
  
  /**
   * Constructor
   * @param identifier
   * @param value
   */
  public TestCompletedTask(int identifier, double value) {
    super(identifier) ;
    value_ = value ;
  } // TestCompletedTask
} // TestCompletedTask
