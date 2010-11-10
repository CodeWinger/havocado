package Commands.RMICommands;

import ResInterface.*;

public class QueryFlightPriceRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNumber;
  
  public ReturnTuple<Integer> price;

  public QueryFlightPriceRMICommand(ResourceManager pRm, int pId, int pFlightNumber) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNumber = pFlightNumber;
    price = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  price = rm.queryFlightPrice(id, flightNumber, timestamp);
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
