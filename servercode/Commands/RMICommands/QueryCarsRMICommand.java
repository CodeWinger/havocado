package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class QueryCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> numCars;

  public QueryCarsRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, String pLocation) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    numCars = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  numCars = rm.queryCars(id, location, timestamp);
	  numCars.timestamp.stamp();
	  setTimestamp(numCars.timestamp);
  }
  
  public void undoCommand() {
	  // do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
