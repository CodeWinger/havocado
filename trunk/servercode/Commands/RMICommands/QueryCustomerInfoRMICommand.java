package Commands.RMICommands;

import ResInterface.*;

public class QueryCustomerInfoRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  
  public ReturnTuple<String> customerInfo;

  public QueryCustomerInfoRMICommand(
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
    
    customerInfo = new ReturnTuple<String>("", null);
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
