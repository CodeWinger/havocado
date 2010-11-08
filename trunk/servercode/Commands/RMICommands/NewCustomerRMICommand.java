package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  
  public int customer;

  public NewCustomerRMICommand(		
  	ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm, 
		int pId)
  {
   	super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;
    
    // Store our attributes.
    id = pId;
    
    customer = -1;
  }
  
  public void doCommand() throws Exception {
      carRm.newCustomer(id);
			roomRm.newCustomer(id);
      customer = flightRm.newCustomer(id);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }
}
