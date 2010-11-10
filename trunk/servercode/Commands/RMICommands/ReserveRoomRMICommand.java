package Commands.RMICommands;

import ResInterface.*;

public class ReserveRoomRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public String location;
  
  public ReturnTuple<Boolean> success;

  public ReserveRoomRMICommand(ResourceManager pRm, int pId, int pCustomer, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    location = pLocation;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  success = rm.reserveRoom(id, customer, location, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
  }
  
  public void undo() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  
			  ReturnTuple<Object> r = rm.unreserveRoom(id, customer, location, timestamp);
			  setTimestamp(r.timestamp);
			  
			  timestamp.stamp();
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
