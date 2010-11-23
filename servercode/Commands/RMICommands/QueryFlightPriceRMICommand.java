package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class QueryFlightPriceRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNumber;
  
  public ReturnTuple<Integer> price;

  public QueryFlightPriceRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, int pFlightNumber) {
    super(pRmGroup);
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
