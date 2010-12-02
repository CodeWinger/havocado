package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class NewCustomerWithIdRMICommand extends AbstractMultiRMICommand {
	/*
  public LinkedList<MemberInfo> carRmGroup;
  public LinkedList<MemberInfo> flightRmGroup;
  public LinkedList<MemberInfo> roomRmGroup;
	
  public transient ResourceManager carRm;
  public transient ResourceManager flightRm;
  public transient ResourceManager roomRm;
	 */
  public int id;
  public int cid;
  
  public ReturnTuple<Boolean> success;

  public NewCustomerWithIdRMICommand(
		LinkedList<MemberInfo> pCarRmGroup, 
		LinkedList<MemberInfo> pFlightRmGroup,
		LinkedList<MemberInfo> pRoomRmGroup,
		int pId, int pCid) 
	{
    super(pCarRmGroup, pFlightRmGroup, pRoomRmGroup); // initialize the abstract constructor - this is only to set the error code to false.
    /*
    carRmGroup = pCarRmGroup;
    flightRmGroup = pFlightRmGroup;
    roomRmGroup = pRoomRmGroup;
    */
    
    // Store our attributes.
    id = pId;
    cid = pCid;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  /*
  protected void populateResourceManagers() throws Exception {
	  ResourceManager c = getAvailableRM(carRmGroup);
	  ResourceManager f = getAvailableRM(flightRmGroup);
	  ResourceManager r = getAvailableRM(roomRmGroup);
	  if (c == null || f == null || r == null) {
		  throw new Exception("One resource manager is unavailable");
	  }
	  carRm = c;
	  flightRm = f;
	  roomRm = r;
  }
  */
  
  public void doCommand() throws Exception {
  		timestamp.stamp();
  		success.result = true;
  		
  		ReturnTuple<Boolean> c = carRm.newCustomer(id, cid, timestamp);
  		setTimestamp(c.timestamp);
  		
      	ReturnTuple<Boolean> f = flightRm.newCustomer(id, cid, timestamp);
      	setTimestamp(f.timestamp);
      	
      	ReturnTuple<Boolean> r = roomRm.newCustomer(id, cid, timestamp);
      	setTimestamp(r.timestamp);
      
      	success.result = c.result && f.result && r.result;
      	
		timestamp.stamp();
		success.timestamp = timestamp;
  }
  
  public void undoCommand() {
	  try {
	  	if(success.result) {
	  		timestamp.stamp();
	  		
	  		ReturnTuple<Boolean> c = carRm.deleteCustomer(id, cid, timestamp);
	  		setTimestamp(c.timestamp);
	  		
	  		ReturnTuple<Boolean> f = flightRm.deleteCustomer(id, cid, timestamp);
	  		setTimestamp(f.timestamp);
	  		
	  		ReturnTuple<Boolean> r = roomRm.deleteCustomer(id, cid, timestamp);
	  		setTimestamp(r.timestamp);
	  		
	  		timestamp.stamp();
	  	}
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
