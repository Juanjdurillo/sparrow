/**
 * JMetalDriverSharedObjects.java
 *
 * @author Antonio J. Nebro
 * @authr Juan J. Durillo
 * @version 1.0
 */
package sparrow.util;

import java.util.*;

import sparrow.worker.WorkerId;

public class SharedObjects {
  public SynchronizedList<Task>           nonScheduledTask_     ;
  public SynchronizedList<CompletedTask>  completedTask_        ;
  public SynchronizedMap<Task>            scheduledTask_        ;
  public SynchronizedList<Message>        receivedMessagesList_ ;
  public SynchronizedList<WorkerId>       idleWorkers_          ;
  public SynchronizedMap<WorkerId>        busyWorkers_          ;
  public List<WorkerId>                   allWorkers_           ;
  public Map<WorkerId,WorkerStatistics>   differentWorkers_     ;
  
  public int      numberOfKnownWorkers_ ;
  public boolean  terminate_ = false;
  public InitData initData_ ;
  public int      sessionId_;

  public boolean terminateReceiver_ ;
  /**
   * Stores the date when starting the computation
   */
  public Date startTime_ ;
  
  /**
   * Stores the date when finishing the computation
   */
  public Date endTime_   ;
  
  /**
   * Constructor
   */
  public SharedObjects() {
    nonScheduledTask_     = new SynchronizedList<Task>() ;
    completedTask_        = new SynchronizedList<CompletedTask>();
    scheduledTask_        = new SynchronizedMap<Task>();
    receivedMessagesList_ = new SynchronizedList<Message>() ;// Not used yet
    idleWorkers_          = new SynchronizedList<WorkerId>();
    busyWorkers_          = new SynchronizedMap<WorkerId>();
    allWorkers_           = new LinkedList<WorkerId>();
    differentWorkers_    = new HashMap<WorkerId,WorkerStatistics>();
    
    numberOfKnownWorkers_ = 0    ;
    initData_             = null ;
    
    sessionId_ = (new java.util.Random()).nextInt();
    
    terminateReceiver_ = false ;
  } // Constructor
  
  /**
   * Returns true if the termination condition holds
   * @return true if the termination have been detected, false otherwise
   */
  synchronized public boolean terminationDetection() {
    if ((nonScheduledTask_.size() == 0) &&
        (scheduledTask_.size() == 0) && 
        (completedTask_.size() == 0)) {
      terminate_ = true ;
      completedTask_.terminate() ;
      nonScheduledTask_.terminate();
      scheduledTask_.terminate();
      return true ;
    }
    else
    	return false ;
  }
} // SharedObjects
