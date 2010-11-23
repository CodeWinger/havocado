package Commands.RMICommands;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Vector;

import ResInterface.*;

public class QueryCustomerInfoRMICommand extends AbstractMultiRMICommand {
/*
  public LinkedList<MemberInfo> carRmGroup;
  public LinkedList<MemberInfo> flightRmGroup;
  public LinkedList<MemberInfo> roomRmGroup;
  
  private ResourceManager carRm;
  private ResourceManager flightRm;
  private ResourceManager roomRm;
  */
  public int id;
  public int customer;
  
  public ReturnTuple<String> customerInfo;

  public QueryCustomerInfoRMICommand(
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
    
    customerInfo = new ReturnTuple<String>("", null);
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
  
  public Vector<String> getCustomerCarReservations() throws RemoteException {
	  timestamp.stamp();
	  
	  ReturnTuple<Vector<String>> r = carRm.customerCarReservations(id, customer, timestamp);
	  setTimestamp(r.timestamp);
	  timestamp.stamp();
	  
	  return r.result;
  }
  
  public Vector<String> getCustomerRoomReservations() throws RemoteException {
	  timestamp.stamp();
	  
	  ReturnTuple<Vector<String>> r = roomRm.customerRoomReservations(id, customer, timestamp);
	  setTimestamp(r.timestamp);
	  timestamp.stamp();
	  
	  return r.result;
  }
  
  public Vector<Integer> getCustomerFlightReservations() throws RemoteException {
	  timestamp.stamp();
	  
	  ReturnTuple<Vector<Integer>> r = flightRm.customerFlightReservations(id, customer, timestamp);
	  setTimestamp(r.timestamp);
	  timestamp.stamp();
	  
	  return r.result;
  }
  
  public void doCommand() throws Exception {
	  	timestamp.stamp();
  		
	  	ReturnTuple<String> carCustomer = carRm.queryCustomerInfo(id, customer, timestamp);
  		setTimestamp(carCustomer.timestamp);
	  	
  		ReturnTuple<String> flightCustomer = flightRm.queryCustomerInfo(id, customer, timestamp);
  		setTimestamp(flightCustomer.timestamp);
  		
      	ReturnTuple<String> roomCustomer = roomRm.queryCustomerInfo(id, customer, timestamp);
      	setTimestamp(roomCustomer.timestamp);
      	
	    customerInfo.result = "Car: " + carCustomer.result + "\n" + "Room: " + roomCustomer.result + "\n" + "Flight: " + flightCustomer.result;
	    
	    timestamp.stamp();
	    customerInfo.timestamp = timestamp;
  }
  
  public void undo() {
	  	// do nothing.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.READ;
	}
}
