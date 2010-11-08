package Commands.RMICommands;

import ResInterface.*;

public class DeleteCustomerRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int customer;
  
  public boolean success;

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
    
    success = false;
  }
  
  public void doCommand() throws Exception {
  		success = true;
      success = success && carRm.deleteCustomer(id, customer);
      success = success && flightRm.deleteCustomer(id, customer);
      success = success && roomRm.deleteCustomer(id, customer);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
