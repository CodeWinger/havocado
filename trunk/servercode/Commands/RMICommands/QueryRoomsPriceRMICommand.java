package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class QueryRoomsPriceRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> price;

  public QueryRoomsPriceRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, String pLocation) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    price = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  price = rm.queryRoomsPrice(id, location, timestamp);
	  price.timestamp.stamp();
	  setTimestamp(price.timestamp);
  }
  
  public void undo() {
	  // do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
