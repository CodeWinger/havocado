package Commands.RMICommands;

import ResInterface.*;

public class AddRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  public int numRooms;
  public int price;

  public ReturnTuple<Boolean> success;

  public AddRoomsRMICommand(ResourceManager pRm, int pId, String pLocation, int pNumRooms, int pPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    numRooms = pNumRooms;
    price = pPrice;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  previousQty = rm.queryRooms(id, location, timestamp).result;
	  previousPrice = rm.queryRoomsPrice(id, location, timestamp).result;
	  success = rm.addRooms(id, location, numRooms, price, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
	  //success = rm.addRooms(id, location, numRooms, price, null).result;
  }
  
  public void undo() {
	  try {
		  timestamp.stamp();
		  ReturnTuple<Object> r = rm.setRooms(id, location, previousQty, previousPrice, timestamp);
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
