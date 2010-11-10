package Commands.RMICommands;

import ResInterface.*;

public class NewCustomerWithIdRMICommand extends AbstractRMICommand {

  public ResourceManager carRm;
  public ResourceManager flightRm;
	public ResourceManager roomRm;

  public int id;
  public int cid;
  
  public boolean success;

  public NewCustomerWithIdRMICommand(
  	ResourceManager pCarRm, 
		ResourceManager pFlightRm, 
		ResourceManager pRoomRm,  
		int pId, int pCid) 
	{
    super(pCarRm); // initialize the abstract constructor - this is only to set the error code to false.
    carRm = pCarRm;
    flightRm = pFlightRm;
    roomRm = pRoomRm;
    
    // Store our attributes.
    id = pId;
    cid = pCid;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
  		success = true;
      success = success && carRm.newCustomer(id, cid, null).result;  // TODO: TIMESTAMP LOGIC.
      success = success && flightRm.newCustomer(id, cid, null).result;  // TODO: TIMESTAMP LOGIC.
      success = success && roomRm.newCustomer(id, cid, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
