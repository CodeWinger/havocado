package Commands.RMICommands;

import ResInterface.*;

public class AddFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNum;
  public int flightSeats;
  public int flightPrice;

  public ReturnTuple<Boolean> success;

  public AddFlightRMICommand(ResourceManager pRm, int pId, int pFlightNum, int pFlightSeats, int pFlightPrice) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
    flightSeats = pFlightSeats;
    flightPrice = pFlightPrice;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
    timestamp.stamp();
    previousQty = rm.queryFlight(id, flightNum, timestamp).result;
    previousPrice = rm.queryFlightPrice(id, flightNum, timestamp).result;
    success = rm.addFlight(id, flightNum, flightSeats, flightPrice, timestamp);
    success.timestamp.stamp();
    setTimestamp(success.timestamp);
  }
  
  public void undo() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  ReturnTuple<Object> r = rm.setFlight(id, flightNum, previousQty, previousPrice, timestamp);
			  r.timestamp.stamp();
			  setTimestamp(r.timestamp);
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
