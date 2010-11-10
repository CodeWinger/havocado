package Commands.RMICommands;

import ResInterface.*;

public class ReserveRoomRMICommand extends AbstractRMICommand {

  public int id;
  public int customer;
  public String location;
  
  public boolean success;

  public ReserveRoomRMICommand(ResourceManager pRm, int pId, int pCustomer, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    customer = pCustomer;
    location = pLocation;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.reserveRoom(id, customer, location, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public int getRequiredLock() {
		return LockManager.LockManager.WRITE;
	}
}
