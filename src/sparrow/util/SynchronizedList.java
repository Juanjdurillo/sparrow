/**
 * JMetalList.java
 *
 * @author Antonio J. Nebro
 * @authr Juan J. Durillo 
 * @version 1.0
 */
package sparrow.util;

import java.util.LinkedList;
import java.util.List;

/**
 * This class implements a concurrent list of objects
 */
public class SynchronizedList<E> {
  List<E> list_      ; // the list
  boolean terminate_ ; // if true, delete the list object
  
  /**
   * Constructor
   */
  SynchronizedList() {
    list_      = new LinkedList<E>() ; 
    terminate_ = false            ;
  } // Constructor
  
  /**
   * Adds an object to the list. If the terminate() method has been invoked, it
   * does nothing.
   * @param object The object to add
   */
  synchronized public void add(E object) {
    if (!terminate_) {
      list_.add(object) ;
      if (list_.size() == 1)
    	  notifyAll() ;
    } // if
  } // add
  
  /**
   * Gets an object from the list
   * @return The object at the beginning of the list
   * @throws Exception 
   */
  synchronized public E get() throws Exception {
    while ((list_.size() == 0) && (!terminate_))
  	wait() ;
    if (list_.size() > 0)
  	  return list_.get(0) ;
    else // terminate_ == true
      return null ;
  } // get
  
  /**
   * Terminates the list: the blocked threads are woken up, and the list is
   * destroyed.
   */
  synchronized public void terminate() {
    terminate_ = true ;
    list_.clear() ;
    notifyAll() ;
  } // terminate
  
  synchronized public void remove() {
    if ((!terminate_) && list_.size() > 0)
      list_.remove(0);
  }
  
  public int size() {
    return list_.size();
  }
  
} // SynchronizedList
