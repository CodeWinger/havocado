package Commands.RMICommands;

import ResInterface.*;

public class QueryCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> numCars;

  public QueryCarsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
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
  
  public void undo() {
	  // do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
