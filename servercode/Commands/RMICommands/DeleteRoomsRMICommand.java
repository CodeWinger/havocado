package Commands.RMICommands;

import ResInterface.*;

public class DeleteRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Boolean> success;

  public DeleteRoomsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
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
  
  public void undo() {
	  try {
		  timestamp.stamp();
		  ReturnTuple<Boolean> r = rm.addRooms(id, location, previousQty, previousPrice, timestamp);
		  r.timestamp.stamp();
		  setTimestamp(r.timestamp);;
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
