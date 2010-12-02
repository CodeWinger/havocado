package Commands.RMICommands;

import java.util.LinkedList;
import java.util.Vector;

import ResInterface.*;

public class DeleteCustomerRMICommand extends AbstractMultiRMICommand {
/*
  public LinkedList<MemberInfo> carRmGroup;
  public LinkedList<MemberInfo> flightRmGroup;
  public LinkedList<MemberInfo> roomRmGroup;
  
  public transient ResourceManager carRm;
  public transient ResourceManager flightRm;
  public transient ResourceManager roomRm;
*/
  public int id;
  public int customer;
  
  public ReturnTuple<Boolean> success;

  public DeleteCustomerRMICommand(
		  LinkedList<MemberInfo> pCarRmGroup,
		  LinkedList<MemberInfo> pFlightRmGroup,
		  LinkedList<MemberInfo> pRoomRmGroup,
		  int pId, int pCustomer) 
	{
    super(pCarRmGroup, pFlightRmGroup, pRoomRmGroup); // initialize the abstract constructor - this is only to set the error code to false.
    /*
    carRmGroup = pCarRmGroup;
    flightRmGroup = pFlightRmGroup;
    roomRmGroup = pRoomRmGroup;
    */
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    
    success = new ReturnTuple<Boolean>(false, null);
  }
  
  /*
  @Override
  protected void populateResourceManagers() throws Exception {
	  ResourceManager c = getAvailableRM(carRmGroup);
	  ResourceManager f = getAvailableRM(flightRmGroup);
	  ResourceManager r = getAvailableRM(roomRmGroup);
	  if (c == null || f == null || r == null) {
		  throw new Exception("One resource manager is unavailable");
	  }
	  carRm = c;
	  flightRm = f;
	  roomRm = r;
  }
  */
  
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
  
  public void undoCommand() {
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
