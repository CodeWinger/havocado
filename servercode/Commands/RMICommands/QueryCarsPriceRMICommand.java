package Commands.RMICommands;

import ResInterface.*;

public class QueryCarsPriceRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public ReturnTuple<Integer> price;

  public QueryCarsPriceRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    price = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  price = rm.queryCarsPrice(id, location, timestamp);
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
