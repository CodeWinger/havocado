package Commands.RMICommands;

import ResInterface.*;

public class QueryRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> numRooms;

  public QueryRoomsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    numRooms = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  numRooms = rm.queryRooms(id, location, timestamp);
	  numRooms.timestamp.stamp();
	  setTimestamp(numRooms.timestamp);
  }
  
  public void undo() {
	  // do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
