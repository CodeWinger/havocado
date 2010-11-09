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
      carRm.newCustomer(id, null);
			roomRm.newCustomer(id, null);
      customer = flightRm.newCustomer(id, null).result;  // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
