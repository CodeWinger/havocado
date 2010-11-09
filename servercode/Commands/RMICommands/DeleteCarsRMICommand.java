package Commands.RMICommands;

import ResInterface.*;

public class DeleteCarsRMICommand extends AbstractRMICommand {

  public int id;
  public String location;
  
  public boolean success;

  public DeleteCarsRMICommand(ResourceManager pRm, int pId, String pLocation) {
    super(pRm);
    // Store our attributes.
    id = pId;
    location = pLocation;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    success = rm.deleteCars(id, location, null).result; // TODO: TIMESTAMP LOGIC.
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}
