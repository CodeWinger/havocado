package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class DeleteFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNum;

  public ReturnTuple<Boolean> success;

  public DeleteFlightRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, int pFlightNum) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
    
    success = new ReturnTuple<Boolean>(false, null);
  }

  public void doCommand() throws Exception {
	// Perform the command.
	timestamp.stamp();
	previousQty = rm.queryFlight(id, flightNum, timestamp).result;
	previousPrice = rm.queryFlightPrice(id, flightNum, timestamp).result;
	success = rm.deleteFlight(id, flightNum, timestamp);
	success.timestamp.stamp();
	setTimestamp(success.timestamp);
	//success = rm.deleteFlight(id, flightNum, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  ReturnTuple<Boolean> r = rm.addFlight(id, flightNum, previousQty, previousPrice, timestamp);
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

