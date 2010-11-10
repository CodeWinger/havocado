package Commands.RMICommands;

import ResInterface.*;

public class AddCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  public int numCars;
  public int price;
  
  public ReturnTuple<Boolean> success;

  public AddCarsRMICommand(ResourceManager pRm, int pId, String pLocation, int pNumCars, int pPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    numCars = pNumCars;
    price = pPrice;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {  
	  timestamp.stamp();
	  previousQty = rm.queryCars(id, location, timestamp).result;
	  previousPrice = rm.queryCarsPrice(id, location, timestamp).result;
	  success = rm.addCars(id, location, numCars, price, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
  }
  
  public void undo() {
	  try {
		  timestamp.stamp();
		  ReturnTuple<Object> r = rm.setCars(id, location, previousQty, previousPrice, timestamp);
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
