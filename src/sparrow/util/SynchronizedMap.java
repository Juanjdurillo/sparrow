/**
 * JMetalMap.java
 *
 * @author Antonio J. Nebro
 * @authr Juan J. Durillo 
 * @version 1.0
 */
package sparrow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a concurrent list of objects
 */
public class SynchronizedMap<E> {
  Map<Integer,E> map_      ; // the list
  boolean        terminate_ ; // if true, delete the list object
  
  /**
   * Constructor
   */
  public SynchronizedMap() {
    map_      = new HashMap<Integer,E>() ; 
    terminate_ = false            ;
  } // Constructor
  
  /**
   * Adds an object to the map. If the terminate() method has been invoked, it
   * does nothing.
   * @param object The object to add
   */
  synchronized public void add(Integer key, E object) {
    if (!terminate_) {
      map_.put(key,object) ;
    }
  } // add
  
  /**
   * Gets an object from the list
   * @return The object at the beginning of the list
   * @throws Exception 
   */
  synchronized public E get(Integer key) throws Exception {
    return map_.get(key);
  } // get
  
  /**
   * Terminates the list: the blocked threads are woken up, and the list is
   * destroyed.
   */
  synchronized public void terminate() {
    terminate_ = true ;
    notifyAll() ;
    map_.clear();
  } // terminate
  
  /**
   * Removes an element from the list
   */
  synchronized public void remove(Integer key) {
    map_.remove(key);
  }
  
  /**
   * @return The size of the map
   */
  public int size() {
    return map_.size();
  } // size
  
  
  /**
   * @return an Iterator<Integer> with the keys
   *
   */
  synchronized public Iterator<Integer> keys() {
    // With this senteces we not allow free access to map_ by the iterator
    Iterator<Integer> iterator = map_.keySet().iterator();
    List<Integer> aux = new ArrayList(map_.keySet().size());
    
    while (iterator.hasNext()) 
      aux.add(iterator.next());
    
    return aux.iterator();
  }
} // JMetalList
