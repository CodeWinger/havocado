package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class AddRoomsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  public int numRooms;
  public int price;

  public ReturnTuple<Boolean> success;

  public AddRoomsRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, String pLocation, int pNumRooms, int pPrice) {
    super(pRmGroup);
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
  
  public void undoCommand() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  
			  if(previousQty == 0) {
				  ReturnTuple<Boolean> r = rm.deleteRooms(id, location, timestamp);
				  r.timestamp.stamp();
				  setTimestamp(r.timestamp);
			  } else {
				  ReturnTuple<Object> r = rm.setRooms(id, location, previousQty, previousPrice, timestamp);
				  r.timestamp.stamp();
				  setTimestamp(r.timestamp);
			  }
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
