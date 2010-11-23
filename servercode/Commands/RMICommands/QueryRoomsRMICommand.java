package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class QueryRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> numRooms;

  public QueryRoomsRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, String pLocation) {
    super(pRmGroup);
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
