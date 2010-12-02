package Commands.RMICommands;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;

import Commands.*;
import ResInterface.*;
import LockManager.LockManager;
import ResImpl.GroupMember;

public abstract class AbstractRMICommand implements Command, Serializable {

// protected transient ResourceManager rm;
  protected LinkedList<MemberInfo> rmGroup;
  protected transient ResourceManager rm;
  protected boolean error;
  
  protected int previousQty = 0;
  protected int previousPrice = 0;
  protected Timestamp timestamp;
  
  public AbstractRMICommand(LinkedList<MemberInfo> pRmGroup) {
    rmGroup = pRmGroup;
    error = false;
  }
  
  /**
   * Get an available resource manager within the provided list.
   * Returns null if no resource manager is available.
   * @param memberList
   * @return
   */
  public static ResourceManager getAvailableRM(LinkedList<MemberInfo> memberList) {
	  int listSize = memberList.size();
	  System.out.println("member list size: " + listSize);
	  for(int i = 0; i < listSize; i++) {
		  MemberInfo mi = memberList.get(i);
		  ResourceManager rm = GroupMember.memberInfoToResourceManager(memberList.get(i));
		  if(rm != null) {
			  try{
				  // Test to see if it's alive.
				  rm.poke();
				  //System.out.println("rm successfully poked.");
				  // no exception was thrown! return!
				  return rm;
			  } catch(RemoteException e) {
				  //System.out.println(mi.rmiName + " is dead.");
				  continue;
			  }
		  }
	  }
	  return null;
  }
  
  /**
   * Populate the resource manager of this command with an available one, 
   * based on the MemberInfo list passed in the constructor.
   * @throws Exception
   */
  protected void populateResourceManagers() throws Exception {
	  ResourceManager r = getAvailableRM(rmGroup);
	  if (r == null) {
		  throw new Exception("Unavailable resource manager");
	  }
	  rm = getAvailableRM(rmGroup);
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
  
  /**
   * Perform the command with the populated resource managers.
   * @throws Exception
   */
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
    	
    	// Populate our resource managers at time of execution.
    	populateResourceManagers();
    	
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
  
  public void undo() {
	  try {
		  // populate my resource managers if they don't exist.
		  if(this.rm == null) {
			  this.populateResourceManagers();
		  }
		  // undo the command.
		  this.undoCommand();
	  } catch (Exception e) {
		  // do nothing... you did your best.
	  }
  }
}
