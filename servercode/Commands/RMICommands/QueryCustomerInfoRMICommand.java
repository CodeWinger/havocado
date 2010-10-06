package Commands.RMICommands;

import ResInterface.*;

public class QueryCustomerInfoRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  
  public String customerInfo;

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
    
    customerInfo = null;
  }
  
  public void doCommand() throws Exception {
  		String carCustomer = carRm.queryCustomerInfo(id, customer);
      String flightCustomer = flightRm.queryCustomerInfo(id, customer);
      String roomCustomer = roomRm.queryCustomerInfo(id, customer); 
	    customerInfo = "Car: " + carCustomer + "\n" + "Room: " + roomCustomer + "\n" + "Flight: " + flightCustomer;
  }
}
