package Commands.RMICommands;

import java.util.LinkedList;

import ResInterface.*;

public class ReserveFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public int flightNumber;
  
  public ReturnTuple<Boolean> success;

  public ReserveFlightRMICommand(LinkedList<MemberInfo> pRmGroup, int pId, int pCustomer, int pFlightNumber) {
    super(pRmGroup);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    flightNumber = pFlightNumber;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  success = rm.reserveFlight(id, customer, flightNumber, timestamp);
	  success.timestamp.stamp();
	  setTimestamp(success.timestamp);
  }
  
  public void undoCommand() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  
			  ReturnTuple<Object> r = rm.unreserveFlight(id, customer, flightNumber, timestamp);
			  setTimestamp(r.timestamp);
			  
			  timestamp.stamp();
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
