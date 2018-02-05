/**
 * Message.java
 *
 * @author Juanjo Durillo
 * @author Antonio J. Nebro
 */

package sparrow.util;

import java.io.Serializable;

public class Message implements Serializable {

  /**
   * List of all the possible messages that a worker can receive
   */
  public static final int ERROR            = -1,
                          STATUS           = 0, 
                          INIT_SESSION     = 1,
                          EXECUTE          = 2,
                          RESET            = 3,
                          END_SESSION      = 4,
                          TERMINATE        = 5,
                          STATUS_ACK       = 6,
                          INIT_SESSION_ACK = 7,
                          EXECUTE_ACK      = 8,
                          RESET_ACK        = 9,                          
                          END_SESSION_ACK  = 10,
                          TERMINATE_ACK    = 11,
                          COMPLETED_TASK   = 12;
  
  /**
   * List of all the possible messages between a driver and a workers server
   */
  public static final int GET_ALL = 0, 
                          GET_N = 1, 
                          END_WORKER_SESSIONS = 2, 
                          ACK = 3, 
                          NACK = 4;
  
  
  /**
   * stores the kind of message
   */
  int type_;
  
  /**
   * stores the status of the answer
   */
  int status_;
  
  /**
   * stores the id of the task
   */
  int id_;
  
  /**
   * stores the content of the message
   */
  private Serializable content_;
  
  /**
   * stores the identification of the session
   */
  private int sessionId_;
  
  /**
   * Creates a new Message
   * By default the type of the message is set to 0
   */
  public Message(int sessionId) {
    type_ = 0;
    sessionId_ = sessionId;
  } //Message
  
  /**
   * Creates a new Message with a type and a content
   */
  public Message(int sessionId, int type, Serializable content) {
    sessionId_ = sessionId;
    type_ = type;
    content_ = content;
  } // Message
  
  /**
   * Creates a new Message with a type
   */
  public Message(int sessionId, int type) {
    sessionId_ = sessionId;
    type_ = type;
  } // Message
  
  /** 
   * Get the content of the message 
   */
  public Object getContent() {
    return content_;
  } // getContent
  
  /**
   * Get the type of the message 
   */
  public int getType() {
    return type_;
  } // getType
  
  /**
   * Set the content of the message
   */
  public void setContent(Serializable content) {
    content_ = content;
  } // setContent
  
  /**
   * Set the type of the message
   */
  public void setType(int type) {
    type_ = type;
  } // setType
  
  
  // Comment!!!
  public int getID() {
    return id_;
  }
  
  public void setId(int id) {
    id_ = id;
  }
  
  public int getStatus() {
    return status_;
  }
  
  public void setStatus(int status) {
    status_ = status;
  }

  public int getSessionId() {
    return sessionId_;
  }
} // Message
