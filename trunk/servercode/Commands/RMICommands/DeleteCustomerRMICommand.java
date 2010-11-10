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
  private ReturnTuple<Vector<Integer>> previousFlightReservations;
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
      setTimestamp(rr.timestamp);
      
      success.result = cr.result && fr.result && rr.result;
      
      timestamp.stamp();
      success.timestamp = timestamp;
  }
  
  public void undo() {
	  try {
		  if(success.result) {
			  timestamp.stamp();
			  
			  ReturnTuple<Boolean> cr = carRm.newCustomer(id, customer, timestamp);
			  setTimestamp(cr.timestamp);
			  
			  ReturnTuple<Boolean> fr = flightRm.newCustomer(id, customer, timestamp);
			  setTimestamp(fr.timestamp);
			  
			  ReturnTuple<Boolean> rr = roomRm.newCustomer(id, customer, timestamp);
			  setTimestamp(rr.timestamp);
			  
			  // re-reserve what was lost.
			  ReturnTuple<Boolean> temp;
			  for(String location : previousCarReservations.result){ 
				  temp = carRm.reserveCar(id, customer, location, timestamp);
				  setTimestamp(temp.timestamp);
			  }
			  for(Integer flightNum : previousFlightReservations.result) {
				  temp = flightRm.reserveFlight(id, customer, flightNum, timestamp);
				  setTimestamp(temp.timestamp);
			  }
			  for(String location : previousRoomReservations.result) {
				  temp = roomRm.reserveRoom(id, customer, location, timestamp);
				  setTimestamp(temp.timestamp);
			  }
			  
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
