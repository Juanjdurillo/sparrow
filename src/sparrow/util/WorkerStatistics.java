/**
 * WorkerStatistics.java
 * @author Antonio J. Nebro
 */
package sparrow.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WorkerStatistics implements Serializable {
  //public enum Key {
    //START_SESSION,
    //END_SESSION,
  //  UPDATE_TIME} ;
    
  /*
  public long   benchmark_       ;
  public String architecture_    ;
  public String operatingSystem_ ;
  
  public Date   startTime_      ;
  public Date   currentTime_    ;
  public long   elapsedTime_    ;
  public String elapsedTimeStr_ ; 
  */
  public int    totalNumberOfComputedTasks_ ;
  public long   totalComputingTime_         ;
  /*
  public int    numberOfResets_        ;
  public int    numberOfSessions_      ; // between INIT and RESET messages
  
  public Date   startTimeLastSession_               ;
  public Date   endTimeLastSession_                 ;
  public int    numberOfComputedTasksInLastSession_ ;
  public long   totalComputingTimeInLastSession_    ;
  
  public Date   startTimeLastTask_     ;
  public Date   endTimeLastTask_       ;
  public long   computingTimeLastTask_ ;
  */
  /**
   * Constructor
   */
  public WorkerStatistics () {
    /*
    benchmark_       = 0 ;
    
    architecture_    = System.getProperty("os.arch") ;
    operatingSystem_ = System.getProperty("os.name") ;
    
    startTime_      = new Date() ;
    currentTime_    = null ;
    elapsedTime_    = 0 ;
    elapsedTimeStr_ = "" ;
    */
    totalNumberOfComputedTasks_ = 0 ;
    totalComputingTime_         = 0 ;
    /*
    numberOfSessions_           = 0 ; // between INIT and RESET messages
    
    startTimeLastSession_       = null ;
    endTimeLastSession_         = null ;
    numberOfComputedTasksInLastSession_ = 0 ;
    totalComputingTimeInLastSession_    = 0 ;
    */
    //startTimeLastTask_     = null ;
    //endTimeLastTask_       = null ;
    //computingTimeLastTask_ = 0    ;
  } // StatisticData
  
  /*
  public void updateStatistics(Key key, Object value) { 
    switch (key) {
    //case START_SESSION:
    //  numberOfSessions_ ++ ;
      //startTimeLastSession_ = new Date() ;
    //  numberOfComputedTasksInLastSession_ = 0 ;
    //  totalComputingTimeInLastSession_    = 0 ;
    //  break ;
    //case END_SESSION:
    //  totalComputingTimeInLastSession_ = 0 ;
    //  break ;
    case UPDATE_TIME:
      computingTimeLastTask_ = ((Long) value).longValue();
      numberOfComputedTasksInLastSession_ ++ ;      
      totalNumberOfComputedTasks_ ++ ;
      totalComputingTime_ += computingTimeLastTask_ ;
      totalComputingTimeInLastSession_ += computingTimeLastTask_ ;
      break ;
    } // switch
  } // updateStatistics
  */
  
  public void updateWorkerStatistics(long time) {
    totalNumberOfComputedTasks_ ++ ;
    totalComputingTime_ += time ;
  } //
  
  /**
   * Sets the current time
   * @param time The current time
   */
  //public void currentTime() {
  //  currentTime_ = new Date() ;
  //  elapsedTime_ = currentTime_.getTime() - startTime_.getTime() ;
  //  elapsedTimeStr_ = this.DDHHMMSS(elapsedTime_) ;
  //} // endTime
  
  /**
   * Returns the start time
   * @return The start time
   */
  /*
  public Date getStartTime() {
    return startTime_ ;
  } // getStartTime
  */
  /**
   * Prints the object into a string
   * @return The string
   */
  /*
  public String toString() {
    String string ;
    
    string = "" ;
    string += "" + startTime_ ;
    
    return string ;
  } // toString
  */
  /**
   * Returns a string contaning  milliseconds expressed as days, hours and 
   * seconds
   * @param milliseconds The seconds to convert
   * @return A string 
   */
  public static String DDHHMMSS(long milliseconds) {
    String ddhhmmss ;

    long totalSeconds = milliseconds/1000 ;
    long days, hours, minutes, seconds;
    days = 0 ;
    days = totalSeconds / 86400 ;
    totalSeconds = totalSeconds - (days * 86400) ;
    hours = totalSeconds / 3600;
    totalSeconds = totalSeconds - (hours * 3600);
    minutes = totalSeconds / 60;
    totalSeconds = totalSeconds - (minutes * 60);
    seconds = totalSeconds;
    
    ddhhmmss = "" + days + ":" +
        hours + ":" +
        minutes + ":" + 
        seconds ;
        
    return ddhhmmss ;
  } // DDHHMMSS
  
  /**
   * This method is unfinished
   * @return
   */
  /*
  public long applyBenchmark() {
    double tmp ;
    tmp    = 1.5 ;
    
    Date startTime = new Date() ;
    for (int i = 0; i < 10; i++) 
      for (int j = 0 ; j < 10; j++)
        tmp = tmp*2.0/1.95 ;
    
    Date endTime = new Date() ;
    
    benchmark_ = endTime.getTime() - startTime.getTime() ;
    return (benchmark_) ;
  }
*/
}
