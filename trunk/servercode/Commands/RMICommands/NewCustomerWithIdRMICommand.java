package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerWithIdRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int cid;
  
  public ReturnTuple<Boolean> success;

  public NewCustomerWithIdRMICommand(
  	ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm,  
		int pId, int pCid) 
	{
    super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;
    
    // Store our attributes.
    id = pId;
    cid = pCid;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
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
  
  public void undo() {
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
