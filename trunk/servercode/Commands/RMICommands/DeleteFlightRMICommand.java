package Commands.RMICommands;

import ResInterface.*;

public class DeleteFlightRMICommand extends AbstractRMICommand {

  public int id;
  public int flightNum;

  public boolean success;

  public DeleteFlightRMICommand(ResourceManager pRm, int pId, int pFlightNum) {
    super(pRm);
    // Store our attributes.
    id = pId;
    flightNum = pFlightNum;
    
    success = false;
  }
  
  public void doCommand() throws Exception {
    // Perform the command.
    success = rm.deleteFlight(id, flightNum);
  }
  
  public void undo() {
	  // TODO: undo this operation.
  }

	@Override
	public RequiredLock getRequiredLock() {
		return RequiredLock.WRITE;
	}
}

