package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class DeleteRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Boolean> success;

  public DeleteRoomsRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, String pLocation) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  previousQty = rm.queryRooms(id, location, timestamp).result;
	  previousPrice = rm.queryRoomsPrice(id, location, timestamp).result;
	  success = rm.deleteRooms(id, location, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
    //success = rm.deleteRooms(id, location, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undoCommand() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  ReturnTuple<Boolean> r = rm.addRooms(id, location, previousQty, previousPrice, timestamp);
			  r.timestamp.stamp();
			  setTimestamp(r.timestamp);
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
