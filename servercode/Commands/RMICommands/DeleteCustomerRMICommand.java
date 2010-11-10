package Commands.RMICommands;

import java.util.Vector;

import ResInterface.*;

public class DeleteCustomerRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  
  public ReturnTuple<Boolean> success;

  public DeleteCustomerRMICommand(
		ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm, 
		int pId, int pCustomer) 
	{
    super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;
    
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  
  private ReturnTuple<Vector<String>> previousCarReservations;
  private ReturnTuple<Vector<String>> previousFlightReservations;
  private ReturnTuple<Vector<String>> previousRoomReservations;
  public void doCommand() throws Exception {
	  timestamp.stamp();
	  
	  previousCarReservations = carRm.customerCarReservations(id, customer, timestamp);
      ReturnTuple<Boolean> cr = carRm.deleteCustomer(id, customer, timestamp);
      setTimestamp(cr.timestamp);
      
      previousFlightReservations = flightRm.customerFlightReservations(id, customer, timestamp);
      ReturnTuple<Boolean> fr = flightRm.deleteCustomer(id, customer, timestamp);
      setTimestamp(fr.timestamp);
      
      previousRoomReservations = roomRm.customerRoomReservations(id, customer, timestamp);
      ReturnTuple<Boolean> rr = roomRm.deleteCustomer(id, customer, timestamp);
      rr.timestamp.stamp();
      setTimestamp(rr.timestamp);
      
      success.result = cr.result && fr.result && rr.result;
  }
  
  public void undo() {
	  try {
		  timestamp.stamp();
		  
		  ReturnTuple<Boolean> cr = carRm.newCustomer(id, customer, timestamp);
		  
		  
	  } catch (Exception e) {
		  
	  }
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
