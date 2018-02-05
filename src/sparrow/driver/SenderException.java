/*
 * SenderException.java
 *
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 */

package sparrow.driver;

/**
 * This class defines a new Exception 
 */
public class SenderException extends Exception {
  
  /**
   * Creates a new instance of SenderException
   */
  public SenderException() {
    super();
  } // SenderException
  
  /**
   * Creates a new instance of SenderException
   */
  public SenderException(String msg) {
    super(msg);
  } // SenderException
} // SenderException
