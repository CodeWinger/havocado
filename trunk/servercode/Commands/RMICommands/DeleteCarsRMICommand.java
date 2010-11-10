package Commands.RMICommands;

import ResInterface.*;

public class DeleteCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Boolean> success;

  public DeleteCarsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  previousQty = rm.queryCars(id, location, timestamp).result;
	  previousPrice = rm.queryCarsPrice(id, location, timestamp).result;
	  success = rm.deleteCars(id, location, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
	  //success = rm.deleteCars(id, location, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  try {
		  timestamp.stamp();
		  ReturnTuple<Boolean> r = rm.addCars(id, location, previousQty, previousPrice, timestamp);
		  r.timestamp.stamp();
		  setTimestamp(r.timestamp);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
