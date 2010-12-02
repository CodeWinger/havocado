package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class ReserveRoomRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public String location;
  
  public ReturnTuple<Boolean> success;

  public ReserveRoomRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, int pCustomer, String pLocation) {
    super(pRmGroup);
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
  
  public void undoCommand() {
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
