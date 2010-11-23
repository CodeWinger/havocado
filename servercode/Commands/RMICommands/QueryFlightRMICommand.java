package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class QueryFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNumber;
  
  public ReturnTuple<Integer> numSeats;

  public QueryFlightRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, int pFlightNumber) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    flightNumber = pFlightNumber;
    numSeats = new ReturnTuple<Integer>(-1, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  numSeats = rm.queryFlight(id, flightNumber, timestamp);
	  numSeats.timestamp.stamp();
	  setTimestamp(numSeats.timestamp);
  }
  
  public void undo() {
	  // do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
