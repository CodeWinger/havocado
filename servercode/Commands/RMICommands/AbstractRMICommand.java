package Commands.RMICommands;

import Commands.*;
import ResInterface.*;
import LockManager.LockManager;

public abstract class AbstractRMICommand implements Command {
  protected ResourceManager rm;
  protected boolean error;
  
  protected int previousQty = 0;
  protected int previousPrice = 0;
  protected Timestamp timestamp;
  
  
  public AbstractRMICommand(ResourceManager pRm) {
    rm = pRm;
    error = false;
  }
  
  public Timestamp getTimestamp() {
	  return timestamp;
  }
  
  public void setTimestampObject(Timestamp pTimestamp) {
	  timestamp = pTimestamp;
  }
  
  protected void setTimestamp(Timestamp pTimestamp) {
	  timestamp = pTimestamp;
  }
  
  public abstract int getRequiredLock();
  
  public abstract void doCommand() throws Exception;
  
  public synchronized void waitFor() { 
    try {
      wait();
    } catch (java.lang.InterruptedException e) {
       // Set the error flag.
      error = true;
      
      // Print the exception.
      System.out.println("EXCEPTION:");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
  
  public synchronized void finished() { notifyAll(); }
  
  public boolean error() { return error; }
  
  public void execute() {
    try{
    	if(timestamp == null) {
    		timestamp = new Timestamp();
    	}
    	
      // Perform the command.
      doCommand();
      
    } catch (Exception e) {
      // Set the error flag.
      error = true;
      
      // Print the exception.
      System.out.println("EXCEPTION:");
      System.out.println(e.getMessage());
      e.printStackTrace();
      
    } finally {
      // Signal any thread waiting on this object that we are done.
      finished();
    }
  }
}
